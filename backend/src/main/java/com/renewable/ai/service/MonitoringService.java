package com.renewable.ai.service;

import com.renewable.ai.dto.*;
import com.renewable.ai.entity.Driver;
import com.renewable.ai.entity.DispatchLog;
import com.renewable.ai.entity.FleetOrder;
import com.renewable.ai.repository.DispatchLogRepository;
import com.renewable.ai.repository.DriverRepository;
import com.renewable.ai.repository.FleetOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitoringService {
    
    @Autowired
    private FleetOrderRepository fleetOrderRepository;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private DispatchLogRepository dispatchLogRepository;
    
    private static final List<FleetOrder.OrderStatus> ACTIVE_STATUSES = Arrays.asList(
            FleetOrder.OrderStatus.ASSIGNED,
            FleetOrder.OrderStatus.PICKED_UP,
            FleetOrder.OrderStatus.IN_TRANSIT,
            FleetOrder.OrderStatus.ARRIVED,
            FleetOrder.OrderStatus.UNLOADING
    );
    
    private static final List<FleetOrder.OrderStatus> COMPLETED_STATUSES = Arrays.asList(
            FleetOrder.OrderStatus.COMPLETED,
            FleetOrder.OrderStatus.CANCELLED
    );
    
    public DashboardVO getDashboardStats(Long fleetId) {
        DashboardVO dashboard = new DashboardVO();
        
        List<FleetOrder> allOrders = fleetOrderRepository.findByFleetId(fleetId, Pageable.unpaged()).getContent();
        
        dashboard.setTotalOrders(allOrders.size());
        dashboard.setPendingOrders(allOrders.stream()
                .filter(o -> o.getStatus() == FleetOrder.OrderStatus.PENDING_ASSIGNMENT)
                .count());
        dashboard.setInProgressOrders(allOrders.stream()
                .filter(o -> ACTIVE_STATUSES.contains(o.getStatus()))
                .count());
        dashboard.setCompletedOrders(allOrders.stream()
                .filter(o -> o.getStatus() == FleetOrder.OrderStatus.COMPLETED)
                .count());
        dashboard.setCancelledOrders(allOrders.stream()
                .filter(o -> o.getStatus() == FleetOrder.OrderStatus.CANCELLED)
                .count());
        
        if (dashboard.getTotalOrders() > 0) {
            dashboard.setCompletionRate(
                    (double) dashboard.getCompletedOrders() / dashboard.getTotalOrders() * 100);
        }
        
        List<FleetOrder> completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == FleetOrder.OrderStatus.COMPLETED && o.getCompletedTime() != null)
                .collect(Collectors.toList());
        
        if (!completedOrders.isEmpty()) {
            double avgTime = completedOrders.stream()
                    .mapToLong(o -> java.time.Duration.between(o.getCreatedAt(), o.getCompletedTime()).toHours())
                    .average()
                    .orElse(0);
            dashboard.setAverageProcessingTime(avgTime);
        }
        
        List<Driver> allDrivers = driverRepository.findByFleetId(fleetId, Pageable.unpaged()).getContent();
        dashboard.setTotalDrivers(allDrivers.size());
        dashboard.setActiveDrivers(allDrivers.stream()
                .filter(d -> "AVAILABLE".equals(d.getStatus()))
                .count());
        
        if (dashboard.getTotalDrivers() > 0) {
            dashboard.setDriverUtilizationRate(
                    (double) dashboard.getActiveDrivers() / dashboard.getTotalDrivers() * 100);
        }
        
        dashboard.setDailyStats(calculateDailyStats(allOrders));
        
        return dashboard;
    }
    
    private List<DashboardVO.DailyStatsVO> calculateDailyStats(List<FleetOrder> orders) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        
        Map<String, DashboardVO.DailyStatsVO> statsMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            String date = now.minusDays(i).format(formatter);
            DashboardVO.DailyStatsVO stats = new DashboardVO.DailyStatsVO();
            stats.setDate(date);
            stats.setCreatedOrders(0);
            stats.setCompletedOrders(0);
            stats.setTotalWeight(0.0);
            statsMap.put(date, stats);
        }
        
        for (FleetOrder order : orders) {
            String date = order.getCreatedAt().format(formatter);
            if (statsMap.containsKey(date)) {
                DashboardVO.DailyStatsVO stats = statsMap.get(date);
                stats.setCreatedOrders(stats.getCreatedOrders() + 1);
                if (order.getActualWeight() != null) {
                    stats.setTotalWeight(stats.getTotalWeight() + order.getActualWeight());
                }
            }
            
            if (order.getCompletedTime() != null) {
                String completedDate = order.getCompletedTime().format(formatter);
                if (statsMap.containsKey(completedDate)) {
                    statsMap.get(completedDate).setCompletedOrders(
                            statsMap.get(completedDate).getCompletedOrders() + 1);
                }
            }
        }
        
        return new ArrayList<>(statsMap.values());
    }
    
    public List<DriverStatsVO> getDriverStats(Long fleetId) {
        List<Driver> drivers = driverRepository.findByFleetId(fleetId, Pageable.unpaged()).getContent();
        List<FleetOrder> allOrders = fleetOrderRepository.findByFleetId(fleetId, Pageable.unpaged()).getContent();
        
        Map<Long, List<FleetOrder>> ordersByDriver = allOrders.stream()
                .filter(o -> o.getDriverId() != null)
                .collect(Collectors.groupingBy(FleetOrder::getDriverId));
        
        return drivers.stream().map(driver -> {
            DriverStatsVO stats = new DriverStatsVO();
            stats.setDriverId(driver.getId());
            stats.setDriverName(driver.getName());
            stats.setPhone(driver.getPhone());
            stats.setVehiclePlate(driver.getVehiclePlate());
            stats.setStatus(driver.getStatus());
            
            List<FleetOrder> driverOrders = ordersByDriver.getOrDefault(driver.getId(), Collections.emptyList());
            stats.setTotalOrders(driverOrders.size());
            stats.setCompletedOrders((int) driverOrders.stream()
                    .filter(o -> o.getStatus() == FleetOrder.OrderStatus.COMPLETED)
                    .count());
            stats.setInProgressOrders((int) driverOrders.stream()
                    .filter(o -> ACTIVE_STATUSES.contains(o.getStatus()))
                    .count());
            
            if (stats.getTotalOrders() > 0) {
                stats.setCompletionRate((double) stats.getCompletedOrders() / stats.getTotalOrders() * 100);
            }
            
            List<FleetOrder> completedDriverOrders = driverOrders.stream()
                    .filter(o -> o.getStatus() == FleetOrder.OrderStatus.COMPLETED && o.getCompletedTime() != null)
                    .collect(Collectors.toList());
            
            if (!completedDriverOrders.isEmpty()) {
                double avgTime = completedDriverOrders.stream()
                        .mapToLong(o -> java.time.Duration.between(o.getCreatedAt(), o.getCompletedTime()).toHours())
                        .average()
                        .orElse(0);
                stats.setAverageOrderTime(avgTime);
            }
            
            stats.setCurrentLatitude(driver.getCurrentLatitude());
            stats.setCurrentLongitude(driver.getCurrentLongitude());
            stats.setLastLocationUpdate(driver.getLastLocationUpdate() != null ? 
                    driver.getLastLocationUpdate().toString() : null);
            
            List<OrderVO> currentOrderVOs = driverOrders.stream()
                    .filter(o -> ACTIVE_STATUSES.contains(o.getStatus()))
                    .map(OrderVO::fromEntity)
                    .collect(Collectors.toList());
            stats.setCurrentOrders(currentOrderVOs);
            
            return stats;
        }).collect(Collectors.toList());
    }
    
    @Transactional
    public void updateDriverLocation(Long driverId, Long fleetId, Double latitude, Double longitude) {
        Driver driver = driverRepository.findByIdAndFleetId(driverId, fleetId)
                .orElseThrow(() -> new RuntimeException("司机不存在"));
        
        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver.setLastLocationUpdate(LocalDateTime.now());
        driverRepository.save(driver);
    }
    
    public Page<DispatchLogVO> getDispatchLogs(Long fleetId, Long orderId, String actionType,
                                              LocalDateTime startTime, LocalDateTime endTime,
                                              Pageable pageable) {
        Page<DispatchLog> logs;
        
        if (orderId != null) {
            logs = dispatchLogRepository.findByOrderId(orderId, pageable);
        } else if (startTime != null && endTime != null) {
            logs = dispatchLogRepository.findByActionTimeBetween(startTime, endTime, pageable);
        } else if (actionType != null && !actionType.isEmpty()) {
            try {
                DispatchLog.ActionType type = DispatchLog.ActionType.valueOf(actionType.toUpperCase());
                logs = dispatchLogRepository.findByActionType(type, pageable);
            } catch (IllegalArgumentException e) {
                logs = Page.empty();
            }
        } else {
            logs = dispatchLogRepository.findAll(pageable);
        }
        
        return logs.map(this::convertToVO);
    }
    
    private DispatchLogVO convertToVO(DispatchLog log) {
        DispatchLogVO vo = new DispatchLogVO();
        vo.setId(log.getId());
        vo.setOrderId(log.getOrderId());
        vo.setOrderNumber(log.getOrderNumber());
        vo.setActionType(log.getActionType() != null ? log.getActionType().name() : null);
        vo.setActionDescription(log.getActionDescription());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorName(log.getOperatorName());
        vo.setPreviousDriverName(log.getPreviousDriverName());
        vo.setNewDriverName(log.getNewDriverName());
        vo.setReason(log.getReason());
        vo.setActionTime(log.getActionTime());
        return vo;
    }
    
    @Transactional
    public void recordDispatchLog(Long orderId, String orderNumber, DispatchLog.ActionType actionType,
                                  String description, Long operatorId, String operatorName,
                                  String previousDriver, String newDriver, String reason) {
        DispatchLog log = new DispatchLog();
        log.setOrderId(orderId);
        log.setOrderNumber(orderNumber);
        log.setActionType(actionType);
        log.setActionDescription(description);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setPreviousDriverName(previousDriver);
        log.setNewDriverName(newDriver);
        log.setReason(reason);
        log.setActionTime(LocalDateTime.now());
        
        dispatchLogRepository.save(log);
    }
}
