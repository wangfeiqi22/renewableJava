package com.renewable.ai.controller;

import com.renewable.ai.common.ApiResponse;
import com.renewable.ai.dto.*;
import com.renewable.ai.entity.User;
import com.renewable.ai.service.GrabService;
import com.renewable.ai.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/fleet/grab")
@CrossOrigin(origins = "*")
public class GrabController {
    
    @Autowired
    private GrabService grabService;
    
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<GrabRecordVO>> grabOrder(@PathVariable Long orderId) {
        User currentUser = SecurityUtil.getCurrentUser();
        GrabRecordVO record = grabService.grabOrder(orderId, currentUser.getId(), currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(record));
    }
    
    @GetMapping("/pool")
    public ResponseEntity<ApiResponse<Page<OrderVO>>> getPooledOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = SecurityUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderVO> orders = grabService.getPooledOrders(currentUser.getFleetId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<OrderVO>>> getAvailableOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = SecurityUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderVO> orders = grabService.getAvailableOrders(currentUser.getFleetId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @GetMapping("/rule")
    public ResponseEntity<ApiResponse<GrabRuleVO>> getGrabRule() {
        User currentUser = SecurityUtil.getCurrentUser();
        GrabRuleVO rule = grabService.getGrabRule(currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(rule));
    }
    
    @PostMapping("/rule")
    public ResponseEntity<ApiResponse<GrabRuleVO>> createOrUpdateGrabRule(
            @Valid @RequestBody GrabRuleDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        GrabRuleVO rule = grabService.createOrUpdateGrabRule(
                dto, currentUser.getFleetId(), currentUser.getId(), currentUser.getName());
        return ResponseEntity.ok(ApiResponse.success(rule));
    }
    
    @PostMapping("/pool/{orderId}")
    public ResponseEntity<ApiResponse<Void>> addOrderToPool(@PathVariable Long orderId) {
        User currentUser = SecurityUtil.getCurrentUser();
        grabService.addOrderToPool(orderId, currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @DeleteMapping("/pool/{orderId}")
    public ResponseEntity<ApiResponse<Void>> removeOrderFromPool(@PathVariable Long orderId) {
        User currentUser = SecurityUtil.getCurrentUser();
        grabService.removeOrderFromPool(orderId, currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/records/my")
    public ResponseEntity<ApiResponse<Page<GrabRecordVO>>> getMyGrabRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = SecurityUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<GrabRecordVO> records = grabService.getDriverGrabRecords(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
    
    @GetMapping("/records/order/{orderId}")
    public ResponseEntity<ApiResponse<Page<GrabRecordVO>>> getOrderGrabRecords(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GrabRecordVO> records = grabService.getOrderGrabRecords(orderId, pageable);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
    
    @GetMapping("/stats/my")
    public ResponseEntity<ApiResponse<GrabService.GrabStatsVO>> getMyGrabStats() {
        User currentUser = SecurityUtil.getCurrentUser();
        GrabService.GrabStatsVO stats = grabService.getDriverGrabStats(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
