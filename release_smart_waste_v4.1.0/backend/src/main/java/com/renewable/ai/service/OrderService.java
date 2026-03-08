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

    public Order createOrder(Order order) {
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByCreatorId(userId);
    }

    public Order updateStatus(Long orderId, Integer status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order assignOrder(Long orderId, Long fleetId, Long driverId, Long vehicleId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setFleetId(fleetId);
        order.setDriverId(driverId);
        order.setVehicleId(vehicleId);
        order.setStatus(20); // Assigned
        return orderRepository.save(order);
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
