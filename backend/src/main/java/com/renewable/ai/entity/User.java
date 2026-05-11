package com.renewable.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String role; // individual, property, street, station, fleet, driver, vip, admin

    private Integer status = 0; // 0: Pending, 1: Active, 2: Rejected, 3: Disabled

    // ====== Registration details (for audit & profile) ======
    // 企业/站点类用户
    private String companyName;
    private String address;
    private String contactName;
    private String contactPhone;

    // 司机类用户
    // driverType: A - 车队司机, B - 个人司机
    private String driverType;
    private String vehiclePlate;
    private String idNumber;
    private String driverLicenseNumber;

    // 资质附件（URL）
    // 企业/站点：营业执照 & 法人身份证
    private String companyLicenseUrl;
    private String companyLegalIdCardUrl;

    // 司机：身份证 & 驾驶证扫描件
    private String driverIdCardUrl;
    private String driverLicenseUrl;

    // 审核备注（驳回原因等）
    private String auditRemark;

    private String avatarUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Fleet ID for drivers (null if freelance)
    @Column(name = "fleet_id")
    private Long fleetId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public String getName() {
        return contactName != null && !contactName.isBlank() ? contactName : username;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getDriverType() { return driverType; }
    public void setDriverType(String driverType) { this.driverType = driverType; }
    
    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
    
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    
    public String getDriverLicenseNumber() { return driverLicenseNumber; }
    public void setDriverLicenseNumber(String driverLicenseNumber) { this.driverLicenseNumber = driverLicenseNumber; }
    
    public String getCompanyLicenseUrl() { return companyLicenseUrl; }
    public void setCompanyLicenseUrl(String companyLicenseUrl) { this.companyLicenseUrl = companyLicenseUrl; }
    
    public String getCompanyLegalIdCardUrl() { return companyLegalIdCardUrl; }
    public void setCompanyLegalIdCardUrl(String companyLegalIdCardUrl) { this.companyLegalIdCardUrl = companyLegalIdCardUrl; }
    
    public String getDriverIdCardUrl() { return driverIdCardUrl; }
    public void setDriverIdCardUrl(String driverIdCardUrl) { this.driverIdCardUrl = driverIdCardUrl; }
    
    public String getDriverLicenseUrl() { return driverLicenseUrl; }
    public void setDriverLicenseUrl(String driverLicenseUrl) { this.driverLicenseUrl = driverLicenseUrl; }
    
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getFleetId() { return fleetId; }
    public void setFleetId(Long fleetId) { this.fleetId = fleetId; }
}
