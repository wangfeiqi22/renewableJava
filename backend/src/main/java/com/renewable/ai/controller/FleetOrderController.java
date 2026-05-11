package com.renewable.ai.controller;

import com.renewable.ai.common.ApiResponse;
import com.renewable.ai.dto.*;
import com.renewable.ai.entity.User;
import com.renewable.ai.service.FleetOrderService;
import com.renewable.ai.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/fleet/orders")
@CrossOrigin(origins = "*")
public class FleetOrderController {
    
    @Autowired
    private FleetOrderService fleetOrderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderVO>> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        OrderVO order = fleetOrderService.createOrder(
                dto, currentUser.getFleetId(), currentUser.getId(), currentUser.getName());
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderVO>>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String driverName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String projectName) {
        User currentUser = SecurityUtil.getCurrentUser();
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? 
                                       Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<OrderVO> orders = fleetOrderService.getOrders(
                currentUser.getFleetId(), driverName, status, projectName, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderVO>> getOrderById(@PathVariable Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        OrderVO order = fleetOrderService.getOrderById(id, currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderVO>> updateOrder(
            @PathVariable Long id, @Valid @RequestBody OrderUpdateDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        OrderVO order = fleetOrderService.updateOrder(
                id, dto, currentUser.getFleetId(), currentUser.getId(), currentUser.getName());
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<OrderVO>> assignOrder(
            @PathVariable Long id, @RequestParam Long driverId) {
        User currentUser = SecurityUtil.getCurrentUser();
        OrderVO order = fleetOrderService.assignOrder(
                id, driverId, currentUser.getFleetId(), currentUser.getId(), currentUser.getName());
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @PostMapping("/batch-assign")
    public ResponseEntity<ApiResponse<List<OrderVO>>> batchAssignOrders(
            @Valid @RequestBody OrderAssignDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        List<OrderVO> orders = fleetOrderService.batchAssignOrders(
                dto, currentUser.getFleetId(), currentUser.getId(), currentUser.getName());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @PutMapping("/{id}/reassign")
    public ResponseEntity<ApiResponse<OrderVO>> reassignOrder(
            @PathVariable Long id, @Valid @RequestBody OrderReassignDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        OrderVO order = fleetOrderService.reassignOrder(
                id, dto, currentUser.getFleetId(), currentUser.getId(), currentUser.getName());
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderVO>> updateOrderStatus(
            @PathVariable Long id, @RequestParam String status) {
        User currentUser = SecurityUtil.getCurrentUser();
        OrderVO order = fleetOrderService.updateOrderStatus(id, status, currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        fleetOrderService.deleteOrder(id, currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
