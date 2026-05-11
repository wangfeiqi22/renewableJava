package com.renewable.ai.controller;

import com.renewable.ai.common.ApiResponse;
import com.renewable.ai.dto.DriverCreateDTO;
import com.renewable.ai.dto.DriverUpdateDTO;
import com.renewable.ai.dto.DriverVO;
import com.renewable.ai.entity.User;
import com.renewable.ai.service.DriverService;
import com.renewable.ai.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fleet/drivers")
@CrossOrigin(origins = "*")
public class DriverController {
    
    @Autowired
    private DriverService driverService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<DriverVO>> createDriver(@RequestBody DriverCreateDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        
        DriverVO driver = driverService.createDriver(
                dto, 
                currentUser.getFleetId(), 
                currentUser.getId(), 
                currentUser.getName()
        );
        
        return ResponseEntity.ok(ApiResponse.success(driver));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DriverVO>>> getDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status) {
        
        User currentUser = SecurityUtil.getCurrentUser();
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? 
                                       Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<DriverVO> drivers = driverService.getDrivers(
                currentUser.getFleetId(), name, phone, status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(drivers));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverVO>> getDriverById(@PathVariable Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        
        DriverVO driver = driverService.getDriverById(id, currentUser.getFleetId());
        
        return ResponseEntity.ok(ApiResponse.success(driver));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverVO>> updateDriver(
            @PathVariable Long id,
            @RequestBody DriverUpdateDTO dto) {
        
        User currentUser = SecurityUtil.getCurrentUser();
        
        DriverVO driver = driverService.updateDriver(
                id, 
                dto, 
                currentUser.getFleetId(), 
                currentUser.getId(), 
                currentUser.getName()
        );
        
        return ResponseEntity.ok(ApiResponse.success(driver));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(@PathVariable Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        
        driverService.deleteDriver(
                id, 
                currentUser.getFleetId(), 
                currentUser.getId(), 
                currentUser.getName()
        );
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
