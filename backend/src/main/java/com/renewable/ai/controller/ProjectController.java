package com.renewable.ai.controller;

import com.renewable.ai.common.ApiResponse;
import com.renewable.ai.dto.ProjectCreateDTO;
import com.renewable.ai.dto.ProjectUpdateDTO;
import com.renewable.ai.dto.ProjectVO;
import com.renewable.ai.entity.User;
import com.renewable.ai.service.ProjectService;
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
@RequestMapping("/api/fleet/projects")
@CrossOrigin(origins = "*")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectVO>> createProject(@Valid @RequestBody ProjectCreateDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        ProjectVO project = projectService.createProject(
                dto, currentUser.getFleetId(), currentUser.getId(), currentUser.getName());
        return ResponseEntity.ok(ApiResponse.success(project));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectVO>>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerName) {
        User currentUser = SecurityUtil.getCurrentUser();
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? 
                                       Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<ProjectVO> projects = projectService.getProjects(
                currentUser.getFleetId(), projectName, status, customerName, pageable);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectVO>> getProjectById(@PathVariable Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        ProjectVO project = projectService.getProjectById(id, currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(project));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectVO>> updateProject(
            @PathVariable Long id, @Valid @RequestBody ProjectUpdateDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        ProjectVO project = projectService.updateProject(
                id, dto, currentUser.getFleetId(), currentUser.getId(), currentUser.getName());
        return ResponseEntity.ok(ApiResponse.success(project));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProjectVO>> updateProjectStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        User currentUser = SecurityUtil.getCurrentUser();
        String status = body.get("status");
        ProjectVO project = projectService.updateProjectStatus(id, status, currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(project));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        projectService.deleteProject(id, currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ProjectService.ProjectStatsVO>> getProjectStats() {
        User currentUser = SecurityUtil.getCurrentUser();
        ProjectService.ProjectStatsVO stats = projectService.getProjectStats(currentUser.getFleetId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
