package com.renewable.ai.controller;

import com.renewable.ai.common.ApiResponse;
import com.renewable.ai.dto.DashboardVO;
import com.renewable.ai.dto.DispatchLogVO;
import com.renewable.ai.dto.DriverStatsVO;
import com.renewable.ai.entity.User;
import com.renewable.ai.service.MonitoringService;
import com.renewable.ai.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fleet/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {
    
    @Autowired
    private MonitoringService monitoringService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardVO>> getDashboardStats() {
        User currentUser = SecurityUtil.getCurrentUser();
        DashboardVO dashboard = monitoringService.getDashboardStats(currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
    
    @GetMapping("/drivers/stats")
    public ResponseEntity<ApiResponse<List<DriverStatsVO>>> getDriverStats() {
        User currentUser = SecurityUtil.getCurrentUser();
        List<DriverStatsVO> stats = monitoringService.getDriverStats(currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/drivers/{driverId}/location")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDriverLocation(@PathVariable Long driverId) {
        User currentUser = SecurityUtil.getCurrentUser();
        List<DriverStatsVO> allStats = monitoringService.getDriverStats(currentUser.getFleetId());
        
        DriverStatsVO driverStats = allStats.stream()
                .filter(s -> s.getDriverId().equals(driverId))
                .findFirst()
                .orElse(null);
        
        if (driverStats == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "司机不存在"));
        }
        
        Map<String, Object> location = Map.of(
                "driverId", driverStats.getDriverId(),
                "driverName", driverStats.getDriverName(),
                "latitude", driverStats.getCurrentLatitude() != null ? driverStats.getCurrentLatitude() : 0.0,
                "longitude", driverStats.getCurrentLongitude() != null ? driverStats.getCurrentLongitude() : 0.0,
                "lastUpdate", driverStats.getLastLocationUpdate() != null ? driverStats.getLastLocationUpdate() : "",
                "status", driverStats.getStatus()
        );
        
        return ResponseEntity.ok(ApiResponse.success(location));
    }
    
    @PutMapping("/drivers/{driverId}/location")
    public ResponseEntity<ApiResponse<Void>> updateDriverLocation(
            @PathVariable Long driverId,
            @RequestBody Map<String, Double> location) {
        User currentUser = SecurityUtil.getCurrentUser();
        
        Double latitude = location.get("latitude");
        Double longitude = location.get("longitude");
        
        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "经纬度不能为空"));
        }
        
        monitoringService.updateDriverLocation(driverId, currentUser.getFleetId(), latitude, longitude);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/dispatch-logs")
    public ResponseEntity<ApiResponse<Page<DispatchLogVO>>> getDispatchLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Sort.Direction sortDirection = Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "actionTime"));
        
        Page<DispatchLogVO> logs = monitoringService.getDispatchLogs(
                null, orderId, actionType, startTime, endTime, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
