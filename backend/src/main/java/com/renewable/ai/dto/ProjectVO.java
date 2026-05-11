package com.renewable.ai.dto;

import com.renewable.ai.entity.FleetProject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProjectVO {
    
    private Long id;
    private Long fleetId;
    private String projectName;
    private String projectCode;
    private String status;
    private String statusDescription;
    private String customerName;
    private String customerPhone;
    private String address;
    private Double latitude;
    private Double longitude;
    private String wasteTypes;
    private BigDecimal estimatedMonthlyVolume;
    private LocalDateTime contractStartDate;
    private LocalDateTime contractEndDate;
    private BigDecimal unitPrice;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long completedOrders;
    private BigDecimal totalWeight;
    private String description;
    private String contactPerson;
    private String contactPhone;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ProjectVO fromEntity(FleetProject project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setFleetId(project.getFleetId());
        vo.setProjectName(project.getProjectName());
        vo.setProjectCode(project.getProjectCode());
        vo.setStatus(project.getStatus() != null ? project.getStatus().name() : null);
        vo.setStatusDescription(getStatusDescription(project.getStatus()));
        vo.setCustomerName(project.getCustomerName());
        vo.setCustomerPhone(project.getCustomerPhone());
        vo.setAddress(project.getAddress());
        vo.setLatitude(project.getLatitude());
        vo.setLongitude(project.getLongitude());
        vo.setWasteTypes(project.getWasteTypes());
        vo.setEstimatedMonthlyVolume(project.getEstimatedMonthlyVolume());
        vo.setContractStartDate(project.getContractStartDate());
        vo.setContractEndDate(project.getContractEndDate());
        vo.setUnitPrice(project.getUnitPrice());
        vo.setTotalRevenue(project.getTotalRevenue());
        vo.setTotalOrders(project.getTotalOrders());
        vo.setCompletedOrders(project.getCompletedOrders());
        vo.setTotalWeight(project.getTotalWeight());
        vo.setDescription(project.getDescription());
        vo.setContactPerson(project.getContactPerson());
        vo.setContactPhone(project.getContactPhone());
        vo.setOperatorId(project.getOperatorId());
        vo.setOperatorName(project.getOperatorName());
        vo.setCreatedAt(project.getCreatedAt());
        vo.setUpdatedAt(project.getUpdatedAt());
        return vo;
    }
    
    private static String getStatusDescription(FleetProject.ProjectStatus status) {
        if (status == null) return "";
        switch (status) {
            case ACTIVE: return "进行中";
            case SUSPENDED: return "已暂停";
            case TERMINATED: return "已终止";
            case EXPIRED: return "已过期";
            default: return status.name();
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFleetId() { return fleetId; }
    public void setFleetId(Long fleetId) { this.fleetId = fleetId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getWasteTypes() { return wasteTypes; }
    public void setWasteTypes(String wasteTypes) { this.wasteTypes = wasteTypes; }
    
    public BigDecimal getEstimatedMonthlyVolume() { return estimatedMonthlyVolume; }
    public void setEstimatedMonthlyVolume(BigDecimal estimatedMonthlyVolume) { this.estimatedMonthlyVolume = estimatedMonthlyVolume; }
    
    public LocalDateTime getContractStartDate() { return contractStartDate; }
    public void setContractStartDate(LocalDateTime contractStartDate) { this.contractStartDate = contractStartDate; }
    
    public LocalDateTime getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(LocalDateTime contractEndDate) { this.contractEndDate = contractEndDate; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
    
    public Long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(Long completedOrders) { this.completedOrders = completedOrders; }
    
    public BigDecimal getTotalWeight() { return totalWeight; }
    public void setTotalWeight(BigDecimal totalWeight) { this.totalWeight = totalWeight; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
