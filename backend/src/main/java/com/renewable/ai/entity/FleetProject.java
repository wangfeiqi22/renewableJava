package com.renewable.ai.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_projects", indexes = {
    @Index(name = "idx_project_fleet_id", columnList = "fleet_id"),
    @Index(name = "idx_project_status", columnList = "status")
})
public class FleetProject {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fleet_id", nullable = false)
    private Long fleetId;
    
    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;
    
    @Column(name = "project_code", length = 32)
    private String projectCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status;
    
    @Column(name = "customer_name", length = 100)
    private String customerName;
    
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    @Column(name = "address", length = 200)
    private String address;
    
    @Column(name = "latitude", precision = 10)
    private Double latitude;
    
    @Column(name = "longitude", precision = 10)
    private Double longitude;
    
    @Column(name = "waste_types", length = 500)
    private String wasteTypes;
    
    @Column(name = "estimated_monthly_volume", precision = 10, scale = 2)
    private BigDecimal estimatedMonthlyVolume;
    
    @Column(name = "contract_start_date")
    private LocalDateTime contractStartDate;
    
    @Column(name = "contract_end_date")
    private LocalDateTime contractEndDate;
    
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_revenue", precision = 12, scale = 2)
    private BigDecimal totalRevenue;
    
    @Column(name = "total_orders")
    private Long totalOrders;
    
    @Column(name = "completed_orders")
    private Long completedOrders;
    
    @Column(name = "total_weight", precision = 12, scale = 2)
    private BigDecimal totalWeight;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "contact_person", length = 50)
    private String contactPerson;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "operator_id")
    private Long operatorId;
    
    @Column(name = "operator_name", length = 50)
    private String operatorName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ProjectStatus.ACTIVE;
        }
        if (totalOrders == null) {
            totalOrders = 0L;
        }
        if (completedOrders == null) {
            completedOrders = 0L;
        }
        if (totalWeight == null) {
            totalWeight = BigDecimal.ZERO;
        }
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFleetId() { return fleetId; }
    public void setFleetId(Long fleetId) { this.fleetId = fleetId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    
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
    
    public enum ProjectStatus {
        ACTIVE,
        SUSPENDED,
        TERMINATED,
        EXPIRED
    }
}
