package com.renewable.ai.service;

import com.renewable.ai.dto.*;
import com.renewable.ai.entity.*;
import com.renewable.ai.exception.*;
import com.renewable.ai.repository.*;
import com.renewable.ai.util.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class GrabService {
    
    @Autowired
    private FleetOrderRepository fleetOrderRepository;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private GrabRuleRepository grabRuleRepository;
    
    @Autowired
    private GrabRecordRepository grabRecordRepository;
    
    @Autowired
    private DistributedLock distributedLock;
    
    @Transactional
    public GrabRecordVO grabOrder(Long orderId, Long driverId, Long fleetId) {
        String lockKey = DistributedLock.getOrderLockKey(orderId);
        
        boolean locked = distributedLock.tryLock(lockKey, 5000);
        if (!locked) {
            throw GrabException.lockFailed();
        }
        
        try {
            FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                    .orElseThrow(FleetOrderException::orderNotFound);
            
            if (!order.getIsPooled() || order.getStatus() != FleetOrder.OrderStatus.PENDING_ASSIGNMENT) {
                throw GrabException.orderNotInPool();
            }
            
            Driver driver = driverRepository.findByIdAndFleetId(driverId, fleetId)
                    .orElseThrow(GrabException::driverNotAvailable);
            
            if (!"AVAILABLE".equals(driver.getStatus())) {
                throw GrabException.driverNotAvailable();
            }
            
            GrabRule rule = grabRuleRepository.findByFleetIdAndEnabled(fleetId, true)
                    .stream().findFirst().orElse(null);
            
            if (rule != null) {
                validateGrabRule(driverId, rule);
            }
            
            List<GrabRecord> existingRecords = grabRecordRepository.findByOrderIdAndSuccessOrderByPositionAsc(orderId, true);
            if (!existingRecords.isEmpty() && !Boolean.TRUE.equals(rule != null && rule.getAllowMultipleGrab())) {
                throw GrabException.alreadyGrabbed();
            }
            
            Integer maxPosition = grabRecordRepository.findMaxPositionByOrderId(orderId);
            int newPosition = (maxPosition != null ? maxPosition : 0) + 1;
            
            GrabRecord record = new GrabRecord();
            record.setOrderId(orderId);
            record.setOrderNumber(order.getOrderNumber());
            record.setDriverId(driverId);
            record.setDriverName(driver.getName());
            record.setFleetId(fleetId);
            record.setGrabTime(LocalDateTime.now());
            record.setPosition(newPosition);
            record.setSuccess(true);
            record.setIsWinner(newPosition == 1);
            
            GrabRecord savedRecord = grabRecordRepository.save(record);
            
            if (savedRecord.getIsWinner()) {
                order.setDriverId(driverId);
                order.setDriverName(driver.getName());
                order.setDriverPhone(driver.getPhone());
                order.setStatus(FleetOrder.OrderStatus.ASSIGNED);
                fleetOrderRepository.save(order);
            }
            
            return GrabRecordVO.fromEntity(savedRecord);
            
        } finally {
            distributedLock.unlock(lockKey);
        }
    }
    
    private void validateGrabRule(Long driverId, GrabRule rule) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        
        if (rule.getMaxOrdersPerDriverPerDay() != null) {
            long todayGrabCount = grabRecordRepository.countByDriverIdAndGrabTimeBetween(driverId, startOfDay, endOfDay);
            if (todayGrabCount >= rule.getMaxOrdersPerDriverPerDay()) {
                throw GrabException.maxOrdersReached();
            }
        }
        
        if (rule.getMinIntervalSeconds() != null) {
            LocalDateTime minTime = LocalDateTime.now().minusSeconds(rule.getMinIntervalSeconds());
            Page<GrabRecord> recentRecords = grabRecordRepository.findRecentGrabsByDriver(driverId, minTime, Pageable.unpaged());
            if (!recentRecords.isEmpty()) {
                throw GrabException.tooFrequent();
            }
        }
    }
    
    public Page<OrderVO> getPooledOrders(Long fleetId, Pageable pageable) {
        return fleetOrderRepository.findByFleetIdAndIsPooled(fleetId, true, pageable)
                .map(OrderVO::fromEntity);
    }
    
    public Page<OrderVO> getAvailableOrders(Long fleetId, Pageable pageable) {
        return fleetOrderRepository.findByFleetIdAndStatus(fleetId, FleetOrder.OrderStatus.PENDING_ASSIGNMENT, pageable)
                .map(OrderVO::fromEntity);
    }
    
    public GrabRuleVO getGrabRule(Long fleetId) {
        return grabRuleRepository.findByFleetId(fleetId)
                .map(GrabRuleVO::fromEntity)
                .orElse(null);
    }
    
    @Transactional
    public GrabRuleVO createOrUpdateGrabRule(GrabRuleDTO dto, Long fleetId, Long operatorId, String operatorName) {
        GrabRule rule = grabRuleRepository.findByFleetId(fleetId).orElse(new GrabRule());
        
        rule.setFleetId(fleetId);
        rule.setRuleName(dto.getRuleName());
        rule.setEnabled(dto.getEnabled());
        rule.setMaxOrdersPerDriverPerDay(dto.getMaxOrdersPerDriverPerDay());
        rule.setMinIntervalSeconds(dto.getMinIntervalSeconds());
        rule.setMaxDistanceKm(dto.getMaxDistanceKm());
        rule.setPriorityEnabled(dto.getPriorityEnabled());
        rule.setFirstComeFirstServe(dto.getFirstComeFirstServe());
        rule.setAllowMultipleGrab(dto.getAllowMultipleGrab());
        rule.setAutoAssign(dto.getAutoAssign());
        rule.setDescription(dto.getDescription());
        rule.setOperatorId(operatorId);
        rule.setOperatorName(operatorName);
        
        GrabRule savedRule = grabRuleRepository.save(rule);
        return GrabRuleVO.fromEntity(savedRule);
    }
    
    @Transactional
    public void addOrderToPool(Long orderId, Long fleetId) {
        FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                .orElseThrow(FleetOrderException::orderNotFound);
        
        if (order.getStatus() != FleetOrder.OrderStatus.PENDING_ASSIGNMENT) {
            throw FleetOrderException.invalidStatus(order.getStatus().name(), "IN_POOL");
        }
        
        order.setIsPooled(true);
        fleetOrderRepository.save(order);
    }
    
    @Transactional
    public void removeOrderFromPool(Long orderId, Long fleetId) {
        FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                .orElseThrow(FleetOrderException::orderNotFound);
        
        order.setIsPooled(false);
        fleetOrderRepository.save(order);
    }
    
    public Page<GrabRecordVO> getDriverGrabRecords(Long driverId, Pageable pageable) {
        return grabRecordRepository.findByDriverId(driverId, pageable)
                .map(GrabRecordVO::fromEntity);
    }
    
    public Page<GrabRecordVO> getOrderGrabRecords(Long orderId, Pageable pageable) {
        return grabRecordRepository.findByOrderId(orderId, pageable)
                .map(GrabRecordVO::fromEntity);
    }
    
    public GrabStatsVO getDriverGrabStats(Long driverId) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        
        GrabStatsVO stats = new GrabStatsVO();
        stats.setDriverId(driverId);
        stats.setTotalGrabsToday(grabRecordRepository.countByDriverIdAndGrabTimeBetween(driverId, startOfDay, endOfDay));
        stats.setSuccessGrabsToday(grabRecordRepository.countSuccessByDriverIdAndGrabTimeBetween(driverId, startOfDay, endOfDay));
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Page<GrabRecord> recentGrabs = grabRecordRepository.findRecentGrabsByDriver(driverId, thirtyDaysAgo, Pageable.unpaged());
        
        stats.setTotalGrabs30Days(recentGrabs.getTotalElements());
        stats.setSuccessGrabs30Days(recentGrabs.getContent().stream().filter(GrabRecord::getSuccess).count());
        
        return stats;
    }
    
    public static class GrabStatsVO {
        private Long driverId;
        private long totalGrabsToday;
        private long successGrabsToday;
        private long totalGrabs30Days;
        private long successGrabs30Days;
        
        public Long getDriverId() { return driverId; }
        public void setDriverId(Long driverId) { this.driverId = driverId; }
        
        public long getTotalGrabsToday() { return totalGrabsToday; }
        public void setTotalGrabsToday(long totalGrabsToday) { this.totalGrabsToday = totalGrabsToday; }
        
        public long getSuccessGrabsToday() { return successGrabsToday; }
        public void setSuccessGrabsToday(long successGrabsToday) { this.successGrabsToday = successGrabsToday; }
        
        public long getTotalGrabs30Days() { return totalGrabs30Days; }
        public void setTotalGrabs30Days(long totalGrabs30Days) { this.totalGrabs30Days = totalGrabs30Days; }
        
        public long getSuccessGrabs30Days() { return successGrabs30Days; }
        public void setSuccessGrabs30Days(long successGrabs30Days) { this.successGrabs30Days = successGrabs30Days; }
    }
}
