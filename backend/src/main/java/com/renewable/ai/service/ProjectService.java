package com.renewable.ai.service;

import com.renewable.ai.dto.*;
import com.renewable.ai.entity.FleetProject;
import com.renewable.ai.entity.FleetOrder;
import com.renewable.ai.exception.ProjectException;
import com.renewable.ai.repository.FleetOrderRepository;
import com.renewable.ai.repository.FleetProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class ProjectService {
    
    @Autowired
    private FleetProjectRepository fleetProjectRepository;
    
    @Autowired
    private FleetOrderRepository fleetOrderRepository;
    
    @Transactional
    public ProjectVO createProject(ProjectCreateDTO dto, Long fleetId, Long operatorId, String operatorName) {
        FleetProject project = new FleetProject();
        project.setFleetId(fleetId);
        project.setProjectName(dto.getProjectName());
        project.setProjectCode(dto.getProjectCode());
        project.setCustomerName(dto.getCustomerName());
        project.setCustomerPhone(dto.getCustomerPhone());
        project.setAddress(dto.getAddress());
        project.setLatitude(dto.getLatitude());
        project.setLongitude(dto.getLongitude());
        project.setWasteTypes(dto.getWasteTypes());
        project.setEstimatedMonthlyVolume(dto.getEstimatedMonthlyVolume());
        project.setContractStartDate(dto.getContractStartDate());
        project.setContractEndDate(dto.getContractEndDate());
        project.setUnitPrice(dto.getUnitPrice());
        project.setDescription(dto.getDescription());
        project.setContactPerson(dto.getContactPerson());
        project.setContactPhone(dto.getContactPhone());
        project.setOperatorId(operatorId);
        project.setOperatorName(operatorName);
        project.setStatus(FleetProject.ProjectStatus.ACTIVE);
        
        FleetProject savedProject = fleetProjectRepository.save(project);
        return ProjectVO.fromEntity(savedProject);
    }
    
    public Page<ProjectVO> getProjects(Long fleetId, String projectName, String status, 
                                       String customerName, Pageable pageable) {
        FleetProject.ProjectStatus projectStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                projectStatus = FleetProject.ProjectStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        
        Page<FleetProject> projects = fleetProjectRepository.findByFleetIdWithFilters(
                fleetId, projectName, projectStatus, customerName, pageable);
        return projects.map(ProjectVO::fromEntity);
    }
    
    public ProjectVO getProjectById(Long projectId, Long fleetId) {
        FleetProject project = fleetProjectRepository.findByIdAndFleetId(projectId, fleetId)
                .orElseThrow(ProjectException::projectNotFound);
        return ProjectVO.fromEntity(project);
    }
    
    @Transactional
    public ProjectVO updateProject(Long projectId, ProjectUpdateDTO dto, Long fleetId, 
                                  Long operatorId, String operatorName) {
        FleetProject project = fleetProjectRepository.findByIdAndFleetId(projectId, fleetId)
                .orElseThrow(ProjectException::projectNotFound);
        
        if (dto.getProjectName() != null) project.setProjectName(dto.getProjectName());
        if (dto.getProjectCode() != null) project.setProjectCode(dto.getProjectCode());
        if (dto.getCustomerName() != null) project.setCustomerName(dto.getCustomerName());
        if (dto.getCustomerPhone() != null) project.setCustomerPhone(dto.getCustomerPhone());
        if (dto.getAddress() != null) project.setAddress(dto.getAddress());
        if (dto.getLatitude() != null) project.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) project.setLongitude(dto.getLongitude());
        if (dto.getWasteTypes() != null) project.setWasteTypes(dto.getWasteTypes());
        if (dto.getEstimatedMonthlyVolume() != null) project.setEstimatedMonthlyVolume(dto.getEstimatedMonthlyVolume());
        if (dto.getContractStartDate() != null) project.setContractStartDate(dto.getContractStartDate());
        if (dto.getContractEndDate() != null) project.setContractEndDate(dto.getContractEndDate());
        if (dto.getUnitPrice() != null) project.setUnitPrice(dto.getUnitPrice());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getContactPerson() != null) project.setContactPerson(dto.getContactPerson());
        if (dto.getContactPhone() != null) project.setContactPhone(dto.getContactPhone());
        
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            try {
                FleetProject.ProjectStatus newStatus = FleetProject.ProjectStatus.valueOf(dto.getStatus().toUpperCase());
                validateStatusTransition(project.getStatus(), newStatus);
                project.setStatus(newStatus);
            } catch (IllegalArgumentException ignored) {}
        }
        
        FleetProject savedProject = fleetProjectRepository.save(project);
        return ProjectVO.fromEntity(savedProject);
    }
    
    @Transactional
    public ProjectVO updateProjectStatus(Long projectId, String newStatus, Long fleetId) {
        FleetProject project = fleetProjectRepository.findByIdAndFleetId(projectId, fleetId)
                .orElseThrow(ProjectException::projectNotFound);
        
        FleetProject.ProjectStatus targetStatus;
        try {
            targetStatus = FleetProject.ProjectStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProjectException("INVALID_STATUS", "无效的项目状态: " + newStatus);
        }
        
        validateStatusTransition(project.getStatus(), targetStatus);
        project.setStatus(targetStatus);
        
        FleetProject savedProject = fleetProjectRepository.save(project);
        return ProjectVO.fromEntity(savedProject);
    }
    
    private void validateStatusTransition(FleetProject.ProjectStatus current, FleetProject.ProjectStatus target) {
        boolean isValid = false;
        
        switch (current) {
            case ACTIVE:
                isValid = target == FleetProject.ProjectStatus.SUSPENDED || 
                          target == FleetProject.ProjectStatus.TERMINATED ||
                          target == FleetProject.ProjectStatus.EXPIRED;
                break;
            case SUSPENDED:
                isValid = target == FleetProject.ProjectStatus.ACTIVE || 
                          target == FleetProject.ProjectStatus.TERMINATED;
                break;
            case TERMINATED:
            case EXPIRED:
                isValid = false;
                break;
        }
        
        if (!isValid) {
            throw ProjectException.invalidStatus(current.name(), target.name());
        }
    }
    
    @Transactional
    public void deleteProject(Long projectId, Long fleetId) {
        FleetProject project = fleetProjectRepository.findByIdAndFleetId(projectId, fleetId)
                .orElseThrow(ProjectException::projectNotFound);
        
        if (project.getStatus() == FleetProject.ProjectStatus.ACTIVE || 
            project.getStatus() == FleetProject.ProjectStatus.SUSPENDED) {
            
            List<FleetOrder.OrderStatus> activeStatuses = Arrays.asList(
                    FleetOrder.OrderStatus.ASSIGNED,
                    FleetOrder.OrderStatus.PICKED_UP,
                    FleetOrder.OrderStatus.IN_TRANSIT,
                    FleetOrder.OrderStatus.ARRIVED,
                    FleetOrder.OrderStatus.UNLOADING
            );
            
            long activeOrderCount = fleetOrderRepository.countByProjectIdAndStatusNotIn(projectId, activeStatuses);
            if (activeOrderCount > 0) {
                throw ProjectException.hasActiveOrders();
            }
        }
        
        fleetProjectRepository.delete(project);
    }
    
    public ProjectStatsVO getProjectStats(Long fleetId) {
        ProjectStatsVO stats = new ProjectStatsVO();
        
        List<FleetProject> allProjects = fleetProjectRepository.findByFleetId(fleetId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        stats.setTotalProjects(allProjects.size());
        stats.setActiveProjects((int) allProjects.stream()
                .filter(p -> p.getStatus() == FleetProject.ProjectStatus.ACTIVE).count());
        stats.setSuspendedProjects((int) allProjects.stream()
                .filter(p -> p.getStatus() == FleetProject.ProjectStatus.SUSPENDED).count());
        stats.setTerminatedProjects((int) allProjects.stream()
                .filter(p -> p.getStatus() == FleetProject.ProjectStatus.TERMINATED).count());
        
        BigDecimal totalRevenue = allProjects.stream()
                .map(FleetProject::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue);
        
        BigDecimal totalWeight = allProjects.stream()
                .map(FleetProject::getTotalWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalWeight(totalWeight);
        
        long totalOrders = allProjects.stream()
                .mapToLong(p -> p.getTotalOrders() != null ? p.getTotalOrders() : 0)
                .sum();
        stats.setTotalOrders(totalOrders);
        
        long completedOrders = allProjects.stream()
                .mapToLong(p -> p.getCompletedOrders() != null ? p.getCompletedOrders() : 0)
                .sum();
        stats.setCompletedOrders(completedOrders);
        
        return stats;
    }
    
    public static class ProjectStatsVO {
        private int totalProjects;
        private int activeProjects;
        private int suspendedProjects;
        private int terminatedProjects;
        private BigDecimal totalRevenue;
        private BigDecimal totalWeight;
        private long totalOrders;
        private long completedOrders;
        
        public int getTotalProjects() { return totalProjects; }
        public void setTotalProjects(int totalProjects) { this.totalProjects = totalProjects; }
        
        public int getActiveProjects() { return activeProjects; }
        public void setActiveProjects(int activeProjects) { this.activeProjects = activeProjects; }
        
        public int getSuspendedProjects() { return suspendedProjects; }
        public void setSuspendedProjects(int suspendedProjects) { this.suspendedProjects = suspendedProjects; }
        
        public int getTerminatedProjects() { return terminatedProjects; }
        public void setTerminatedProjects(int terminatedProjects) { this.terminatedProjects = terminatedProjects; }
        
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public BigDecimal getTotalWeight() { return totalWeight; }
        public void setTotalWeight(BigDecimal totalWeight) { this.totalWeight = totalWeight; }
        
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        
        public long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
    }
}
