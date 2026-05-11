package com.renewable.ai.service;

import com.renewable.ai.dto.*;
import com.renewable.ai.entity.Driver;
import com.renewable.ai.entity.FleetOrder;
import com.renewable.ai.exception.DriverException;
import com.renewable.ai.exception.FleetOrderException;
import com.renewable.ai.repository.DriverRepository;
import com.renewable.ai.repository.FleetOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FleetOrderService {
    
    @Autowired
    private FleetOrderRepository fleetOrderRepository;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Transactional
    public OrderVO createOrder(OrderCreateDTO dto, Long fleetId, Long operatorId, String operatorName) {
        FleetOrder order = new FleetOrder();
        order.setFleetId(fleetId);
        order.setProjectId(dto.getProjectId());
        order.setProjectName(dto.getProjectName());
        order.setWasteType(dto.getWasteType());
        order.setEstimatedWeight(dto.getEstimatedWeight());
        order.setPickupAddress(dto.getPickupAddress());
        order.setPickupLatitude(dto.getPickupLatitude());
        order.setPickupLongitude(dto.getPickupLongitude());
        order.setDestinationAddress(dto.getDestinationAddress());
        order.setDestinationLatitude(dto.getDestinationLatitude());
        order.setDestinationLongitude(dto.getDestinationLongitude());
        order.setScheduledPickupTime(dto.getScheduledPickupTime());
        order.setNotes(dto.getNotes());
        order.setOperatorId(operatorId);
        order.setOperatorName(operatorName);
        order.setIsPooled(dto.getIsPooled() != null ? dto.getIsPooled() : false);
        
        if (dto.getPriority() != null && !dto.getPriority().isEmpty()) {
            try {
                order.setPriority(FleetOrder.OrderPriority.valueOf(dto.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                order.setPriority(FleetOrder.OrderPriority.NORMAL);
            }
        } else {
            order.setPriority(FleetOrder.OrderPriority.NORMAL);
        }
        
        order.setStatus(FleetOrder.OrderStatus.PENDING_ASSIGNMENT);
        
        FleetOrder savedOrder = fleetOrderRepository.save(order);
        return OrderVO.fromEntity(savedOrder);
    }
    
    public Page<OrderVO> getOrders(Long fleetId, String driverName, String status, 
                                   String projectName, Pageable pageable) {
        FleetOrder.OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatus = FleetOrder.OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }
        
        Page<FleetOrder> orders = fleetOrderRepository.findByFleetIdWithFilters(
                fleetId, driverName, orderStatus, projectName, pageable);
        return orders.map(OrderVO::fromEntity);
    }
    
    public OrderVO getOrderById(Long orderId, Long fleetId) {
        FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                .orElseThrow(FleetOrderException::orderNotFound);
        return OrderVO.fromEntity(order);
    }
    
    @Transactional
    public OrderVO updateOrder(Long orderId, OrderUpdateDTO dto, Long fleetId, 
                              Long operatorId, String operatorName) {
        FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                .orElseThrow(FleetOrderException::orderNotFound);
        
        if (dto.getProjectId() != null) order.setProjectId(dto.getProjectId());
        if (dto.getProjectName() != null) order.setProjectName(dto.getProjectName());
        if (dto.getWasteType() != null) order.setWasteType(dto.getWasteType());
        if (dto.getEstimatedWeight() != null) order.setEstimatedWeight(dto.getEstimatedWeight());
        if (dto.getPickupAddress() != null) order.setPickupAddress(dto.getPickupAddress());
        if (dto.getPickupLatitude() != null) order.setPickupLatitude(dto.getPickupLatitude());
        if (dto.getPickupLongitude() != null) order.setPickupLongitude(dto.getPickupLongitude());
        if (dto.getDestinationAddress() != null) order.setDestinationAddress(dto.getDestinationAddress());
        if (dto.getDestinationLatitude() != null) order.setDestinationLatitude(dto.getDestinationLatitude());
        if (dto.getDestinationLongitude() != null) order.setDestinationLongitude(dto.getDestinationLongitude());
        if (dto.getScheduledPickupTime() != null) order.setScheduledPickupTime(dto.getScheduledPickupTime());
        if (dto.getNotes() != null) order.setNotes(dto.getNotes());
        
        if (dto.getPriority() != null && !dto.getPriority().isEmpty()) {
            try {
                order.setPriority(FleetOrder.OrderPriority.valueOf(dto.getPriority().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        
        FleetOrder savedOrder = fleetOrderRepository.save(order);
        return OrderVO.fromEntity(savedOrder);
    }
    
    @Transactional
    public OrderVO assignOrder(Long orderId, Long driverId, Long fleetId, 
                              Long operatorId, String operatorName) {
        FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                .orElseThrow(FleetOrderException::orderNotFound);
        
        if (order.getStatus() != FleetOrder.OrderStatus.PENDING_ASSIGNMENT) {
            throw FleetOrderException.invalidStatus(
                    order.getStatus().name(), FleetOrder.OrderStatus.ASSIGNED.name());
        }
        
        Driver driver = driverRepository.findByIdAndFleetId(driverId, fleetId)
                .orElseThrow(FleetOrderException::driverNotFound);
        
        if (!"AVAILABLE".equals(driver.getStatus())) {
            throw FleetOrderException.driverNotAvailable();
        }
        
        List<FleetOrder.OrderStatus> activeStatuses = Arrays.asList(
                FleetOrder.OrderStatus.ASSIGNED,
                FleetOrder.OrderStatus.PICKED_UP,
                FleetOrder.OrderStatus.IN_TRANSIT,
                FleetOrder.OrderStatus.ARRIVED,
                FleetOrder.OrderStatus.UNLOADING
        );
        
        long activeOrderCount = fleetOrderRepository.countByDriverIdAndStatusNotIn(driverId, activeStatuses);
        if (activeOrderCount > 0) {
            throw FleetOrderException.driverHasActiveOrders();
        }
        
        order.setDriverId(driver.getId());
        order.setDriverName(driver.getName());
        order.setDriverPhone(driver.getPhone());
        order.setStatus(FleetOrder.OrderStatus.ASSIGNED);
        order.setOperatorId(operatorId);
        order.setOperatorName(operatorName);
        
        FleetOrder savedOrder = fleetOrderRepository.save(order);
        return OrderVO.fromEntity(savedOrder);
    }
    
    @Transactional
    public List<OrderVO> batchAssignOrders(OrderAssignDTO dto, Long fleetId, 
                                          Long operatorId, String operatorName) {
        Driver driver = driverRepository.findByIdAndFleetId(dto.getDriverId(), fleetId)
                .orElseThrow(FleetOrderException::driverNotFound);
        
        if (!"AVAILABLE".equals(driver.getStatus())) {
            throw FleetOrderException.driverNotAvailable();
        }
        
        List<FleetOrder.OrderStatus> activeStatuses = Arrays.asList(
                FleetOrder.OrderStatus.ASSIGNED,
                FleetOrder.OrderStatus.PICKED_UP,
                FleetOrder.OrderStatus.IN_TRANSIT,
                FleetOrder.OrderStatus.ARRIVED,
                FleetOrder.OrderStatus.UNLOADING
        );
        
        long activeOrderCount = fleetOrderRepository.countByDriverIdAndStatusNotIn(dto.getDriverId(), activeStatuses);
        if (activeOrderCount > 0) {
            throw FleetOrderException.driverHasActiveOrders();
        }
        
        List<FleetOrder> orders = fleetOrderRepository.findByIdInAndFleetId(dto.getOrderIds(), fleetId);
        
        if (orders.size() != dto.getOrderIds().size()) {
            throw FleetOrderException.orderNotFound();
        }
        
        List<OrderVO> result = new ArrayList<>();
        for (FleetOrder order : orders) {
            if (order.getStatus() != FleetOrder.OrderStatus.PENDING_ASSIGNMENT) {
                continue;
            }
            
            order.setDriverId(driver.getId());
            order.setDriverName(driver.getName());
            order.setDriverPhone(driver.getPhone());
            order.setStatus(FleetOrder.OrderStatus.ASSIGNED);
            order.setOperatorId(operatorId);
            order.setOperatorName(operatorName);
            
            FleetOrder savedOrder = fleetOrderRepository.save(order);
            result.add(OrderVO.fromEntity(savedOrder));
        }
        
        return result;
    }
    
    @Transactional
    public OrderVO reassignOrder(Long orderId, OrderReassignDTO dto, Long fleetId, 
                                 Long operatorId, String operatorName) {
        FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                .orElseThrow(FleetOrderException::orderNotFound);
        
        List<FleetOrder.OrderStatus> allowedStatuses = Arrays.asList(
                FleetOrder.OrderStatus.ASSIGNED,
                FleetOrder.OrderStatus.PICKED_UP
        );
        
        if (!allowedStatuses.contains(order.getStatus())) {
            throw FleetOrderException.invalidStatus(
                    order.getStatus().name(), "REASSIGNED");
        }
        
        Driver newDriver = driverRepository.findByIdAndFleetId(dto.getNewDriverId(), fleetId)
                .orElseThrow(FleetOrderException::driverNotFound);
        
        if (!"AVAILABLE".equals(newDriver.getStatus())) {
            throw FleetOrderException.driverNotAvailable();
        }
        
        List<FleetOrder.OrderStatus> activeStatuses = Arrays.asList(
                FleetOrder.OrderStatus.ASSIGNED,
                FleetOrder.OrderStatus.PICKED_UP,
                FleetOrder.OrderStatus.IN_TRANSIT,
                FleetOrder.OrderStatus.ARRIVED,
                FleetOrder.OrderStatus.UNLOADING
        );
        
        long activeOrderCount = fleetOrderRepository.countByDriverIdAndStatusNotIn(dto.getNewDriverId(), activeStatuses);
        if (activeOrderCount > 0) {
            throw FleetOrderException.driverHasActiveOrders();
        }
        
        order.setDriverId(newDriver.getId());
        order.setDriverName(newDriver.getName());
        order.setDriverPhone(newDriver.getPhone());
        order.setOperatorId(operatorId);
        order.setOperatorName(operatorName);
        
        if (order.getNotes() != null && dto.getReason() != null) {
            order.setNotes(order.getNotes() + "\n[换司机原因] " + dto.getReason());
        } else if (dto.getReason() != null) {
            order.setNotes("[换司机原因] " + dto.getReason());
        }
        
        FleetOrder savedOrder = fleetOrderRepository.save(order);
        return OrderVO.fromEntity(savedOrder);
    }
    
    @Transactional
    public OrderVO updateOrderStatus(Long orderId, String newStatus, Long fleetId) {
        FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                .orElseThrow(FleetOrderException::orderNotFound);
        
        FleetOrder.OrderStatus targetStatus;
        try {
            targetStatus = FleetOrder.OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw FleetOrderException.invalidOrderOperation("无效的订单状态: " + newStatus);
        }
        
        validateStatusTransition(order.getStatus(), targetStatus);
        
        order.setStatus(targetStatus);
        
        if (targetStatus == FleetOrder.OrderStatus.PICKED_UP) {
            order.setActualPickupTime(LocalDateTime.now());
        } else if (targetStatus == FleetOrder.OrderStatus.COMPLETED) {
            order.setCompletedTime(LocalDateTime.now());
        }
        
        FleetOrder savedOrder = fleetOrderRepository.save(order);
        return OrderVO.fromEntity(savedOrder);
    }
    
    private void validateStatusTransition(FleetOrder.OrderStatus current, FleetOrder.OrderStatus target) {
        boolean isValid = false;
        
        switch (current) {
            case PENDING_ASSIGNMENT:
                isValid = target == FleetOrder.OrderStatus.ASSIGNED || 
                          target == FleetOrder.OrderStatus.CANCELLED;
                break;
            case ASSIGNED:
                isValid = target == FleetOrder.OrderStatus.PICKED_UP || 
                          target == FleetOrder.OrderStatus.CANCELLED;
                break;
            case PICKED_UP:
                isValid = target == FleetOrder.OrderStatus.IN_TRANSIT;
                break;
            case IN_TRANSIT:
                isValid = target == FleetOrder.OrderStatus.ARRIVED;
                break;
            case ARRIVED:
                isValid = target == FleetOrder.OrderStatus.UNLOADING;
                break;
            case UNLOADING:
                isValid = target == FleetOrder.OrderStatus.COMPLETED;
                break;
            default:
                isValid = false;
        }
        
        if (!isValid) {
            throw FleetOrderException.invalidStatus(current.name(), target.name());
        }
    }
    
    @Transactional
    public void deleteOrder(Long orderId, Long fleetId) {
        FleetOrder order = fleetOrderRepository.findByIdAndFleetId(orderId, fleetId)
                .orElseThrow(FleetOrderException::orderNotFound);
        
        List<FleetOrder.OrderStatus> undeletableStatuses = Arrays.asList(
                FleetOrder.OrderStatus.PICKED_UP,
                FleetOrder.OrderStatus.IN_TRANSIT,
                FleetOrder.OrderStatus.ARRIVED,
                FleetOrder.OrderStatus.UNLOADING
        );
        
        if (undeletableStatuses.contains(order.getStatus())) {
            throw FleetOrderException.invalidOrderOperation(
                    "订单状态为 " + order.getStatus() + "，无法删除");
        }
        
        fleetOrderRepository.delete(order);
    }
}
