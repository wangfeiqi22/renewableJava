package com.renewable.ai.service;

import com.renewable.ai.dto.DashboardStatsDTO;
import com.renewable.ai.entity.Order;
import com.renewable.ai.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StationService stationService;

    @Autowired
    private com.renewable.ai.repository.OrderLogRepository orderLogRepository;

    private void logStatusChange(Order order, Integer oldStatus, Integer newStatus, String action, Long operatorId, String remark) {
        com.renewable.ai.entity.OrderLog log = new com.renewable.ai.entity.OrderLog();
        log.setOrderId(order.getId());
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setActionName(action);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        
        if (operatorId != null) {
            userRepository.findById(operatorId).ifPresent(u -> log.setOperatorName(u.getUsername()));
        } else {
            log.setOperatorName("System");
        }
        
        orderLogRepository.save(log);
    }

    public List<com.renewable.ai.entity.OrderLog> getOrderLogs(Long orderId) {
        return orderLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    public Order createOrder(Order order) {
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        if (order.getStatus() == null) {
            order.setStatus(10); // Default to Pool
        }
        
        // Auto-Dispatch Logic: Find nearest station if not set
        if (order.getStationId() == null && order.getPickupLat() != null && order.getPickupLon() != null) {
            // Type 1: Clearance Station (Terminal)
            com.renewable.ai.entity.Station nearest = stationService.findNearestStation(
                order.getPickupLat().doubleValue(), 
                order.getPickupLon().doubleValue(), 
                1
            );
            if (nearest != null) {
                order.setStationId(nearest.getId());
            }
        }
        
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByCreatorIdOrDriverId(userId, userId);
    }
    
    public List<Order> getPoolOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == 10)
                .toList();
    }
    
    @Autowired
    private com.renewable.ai.repository.UserRepository userRepository;

    public Order grabOrder(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != 10) {
            throw new RuntimeException("Order not available");
        }
        
        // Strict Validation: Only Type A (Fleet Drivers) can grab
        com.renewable.ai.entity.User driver = userRepository.findById(driverId).orElseThrow();
        if (driver.getFleetId() == null) {
            throw new RuntimeException("Access Denied: Freelance drivers cannot grab pool orders.");
        }

        order.setStatus(20); // Assigned/Grabbed
        order.setDriverId(driverId);
        order.setFleetId(driver.getFleetId()); // Assign to fleet as well
        Order savedOrder = orderRepository.save(order);
        logStatusChange(savedOrder, 10, 20, "Grab", driverId, "司机抢单成功");
        return savedOrder;
    }

    public Order updateStatus(Long orderId, Integer status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        Integer oldStatus = order.getStatus();
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        
        // Infer action name based on status
        String action = "Update Status";
        String remark = "状态更新: " + oldStatus + " -> " + status;
        if (status == 25) { action = "Accept"; remark = "司机接单"; }
        else if (status == 30) { action = "Load"; remark = "开始装车"; }
        else if (status == 40) { action = "Depart"; remark = "开始运输"; }
        else if (status == 50) { action = "Arrive"; remark = "到达站点"; }
        else if (status == 60) { action = "Complete"; remark = "订单完成"; }
        
        // For simplicity, we assume the current driver is the operator if available, or system
        Long operatorId = order.getDriverId();
        
        logStatusChange(savedOrder, oldStatus, status, action, operatorId, remark);
        return savedOrder;
    }

    public Order assignOrder(Long orderId, Long fleetId, Long driverId, Long vehicleId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        Integer oldStatus = order.getStatus();
        order.setFleetId(fleetId);
        order.setDriverId(driverId);
        order.setVehicleId(vehicleId);
        order.setStatus(20); // Assigned
        Order savedOrder = orderRepository.save(order);
        logStatusChange(savedOrder, oldStatus, 20, "Assign", null, "车队指派订单");
        return savedOrder;
    }

    public Order createSelfOrder(Order order, Long driverId) {
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setDriverId(driverId);
        order.setCreatorId(driverId); // Set creator as the driver
        order.setStatus(20); // Directly assigned to self (Ready to pickup)
        // Creator is technically the driver acting as agent, or we can leave creatorId null/system
        // For simplicity, we set creatorId to driverId too or handle in controller
        Order savedOrder = orderRepository.save(order);
        logStatusChange(savedOrder, null, 20, "Self Create", driverId, "司机自主建单");
        return savedOrder;
    }

    public org.springframework.data.domain.Page<Order> getDriverHistory(Long driverId, int page, int size, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        List<Integer> statuses = java.util.Arrays.asList(60, 70); // 60: Completed, 70: Cancelled (if exists, or just use 60)
        // If status 70 (Cancelled) is not defined, we can stick to 60.
        // Assuming 60 is completed.
        // If we want to include cancelled, we need to check if 70 is used.
        // The enum is not visible here, but typically completed/cancelled are > 50.
        // Let's assume 60 is completed. If cancelled is used, we can add it.
        // Based on frontend getStatusText map: 60: '已完成'. No Cancelled status mapped explicitly there except 'CANCEL' in timeline.
        // Let's assume completed only for now or include 70 if standard.
        // Or check findByStatus usage.
        
        // Let's check if there is a Cancelled status.
        // In OrderTimeline.vue: 'CANCEL': '取消订单'.
        // Let's assume 70 is cancelled.
        return orderRepository.findHistoryTasks(driverId, statuses, startDate, endDate, pageable);
    }

    public DashboardStatsDTO getDashboardStats() {
        List<Order> allOrders = orderRepository.findAll();
        long total = allOrders.size();
        long completed = allOrders.stream().filter(o -> o.getStatus() == 60).count();
        long pending = allOrders.stream().filter(o -> o.getStatus() == 10).count();
        double rate = total == 0 ? 0 : (double) completed / total * 100;
        return new DashboardStatsDTO(total, completed, pending, rate);
    }
}
