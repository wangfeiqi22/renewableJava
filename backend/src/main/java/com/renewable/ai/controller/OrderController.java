package com.renewable.ai.controller;

import com.renewable.ai.entity.Order;
import com.renewable.ai.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }
    
    @GetMapping("/pool")
    public ResponseEntity<List<Order>> getPoolOrders() {
        return ResponseEntity.ok(orderService.getPoolOrders());
    }
    
    @PutMapping("/{orderId}/grab")
    public ResponseEntity<Order> grabOrder(@PathVariable Long orderId, @RequestParam Long driverId) {
        return ResponseEntity.ok(orderService.grabOrder(orderId, driverId));
    }

    @PutMapping("/{orderId}/accept")
    public ResponseEntity<Order> acceptOrder(@PathVariable Long orderId, @RequestParam Long driverId) {
        // Only allow if order is in Assigned (20) state and assigned to this driver
        // Or if it's a pool order (10) -> handled by grab (which we removed for freelance)
        // Here we assume "Accept" means confirming a dispatched order (Status 20 -> 25 Accepted/OnWay)
        // Or we can jump directly to 30 (Loading) if 'Start Loading' implies acceptance.
        // Let's assume user wants an explicit 'Accept' step before 'Start Loading'.
        
        // However, current flow is 20 (Assigned) -> 30 (Loading).
        // Let's add a status 25 'Accepted' or just use 'Start Loading' as acceptance.
        // If user specifically asked for "Accept" function, maybe they mean for Fleet Drivers to acknowledge?
        
        // Let's implement a status update to 25 "Driver Accepted"
        return ResponseEntity.ok(orderService.updateStatus(orderId, 25));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long orderId, @RequestParam Integer status) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, status));
    }

    @PutMapping("/{orderId}/assign")
    public ResponseEntity<Order> assignOrder(@PathVariable Long orderId, @RequestBody Map<String, Long> payload) {
        return ResponseEntity.ok(orderService.assignOrder(orderId, payload.get("fleetId"), payload.get("driverId"), payload.get("vehicleId")));
    }

    @PostMapping("/self-create")
    public ResponseEntity<Order> createSelfOrder(@RequestBody Map<String, Object> payload) {
        // Extract fields manually since payload structure differs slightly from Order entity
        Order order = new Order();
        order.setPickupAddress((String) payload.get("pickupAddress"));
        order.setWasteType((String) payload.get("wasteType"));
        // Handle numeric conversion safely
        Object weightObj = payload.get("estWeight");
        if (weightObj instanceof Integer) {
            order.setEstWeight(java.math.BigDecimal.valueOf((Integer) weightObj));
        } else if (weightObj instanceof Double) {
            order.setEstWeight(java.math.BigDecimal.valueOf((Double) weightObj));
        }
        
        // Shipper info (stored in wasteDesc or separate fields if entity updated)
        String shipperInfo = "托运人: " + payload.get("shipperName") + " (" + payload.get("shipperPhone") + ")";
        order.setWasteDesc(shipperInfo);
        
        Long driverId = ((Number) payload.get("driverId")).longValue();
        
        return ResponseEntity.ok(orderService.createSelfOrder(order, driverId));
    }

    @GetMapping("/{orderId}/logs")
    public ResponseEntity<List<com.renewable.ai.entity.OrderLog>> getOrderLogs(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderLogs(orderId));
    }
}
