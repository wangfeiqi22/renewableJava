package com.renewable.ai.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DriverVO {
    private Long id;
    private String name;
    private String phone;
    private String idNumber;
    private String driverLicenseNumber;
    private String driverLicenseLevel;
    private String vehiclePlate;
    private String vehicleType;
    private BigDecimal vehicleCapacity;
    private Long fleetId;
    private String fleetName;
    private String status;
    private String statusDescription;
    private LocalDate hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    
    public String getDriverLicenseNumber() { return driverLicenseNumber; }
    public void setDriverLicenseNumber(String driverLicenseNumber) { this.driverLicenseNumber = driverLicenseNumber; }
    
    public String getDriverLicenseLevel() { return driverLicenseLevel; }
    public void setDriverLicenseLevel(String driverLicenseLevel) { this.driverLicenseLevel = driverLicenseLevel; }
    
    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    
    public BigDecimal getVehicleCapacity() { return vehicleCapacity; }
    public void setVehicleCapacity(BigDecimal vehicleCapacity) { this.vehicleCapacity = vehicleCapacity; }
    
    public Long getFleetId() { return fleetId; }
    public void setFleetId(Long fleetId) { this.fleetId = fleetId; }
    
    public String getFleetName() { return fleetName; }
    public void setFleetName(String fleetName) { this.fleetName = fleetName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public static DriverVO fromEntity(com.renewable.ai.entity.Driver driver) {
        DriverVO vo = new DriverVO();
        vo.setId(driver.getId());
        vo.setName(driver.getName());
        vo.setPhone(driver.getPhone());
        vo.setIdNumber(driver.getIdNumber());
        vo.setDriverLicenseNumber(driver.getDriverLicenseNumber());
        vo.setDriverLicenseLevel(driver.getDriverLicenseLevel());
        vo.setVehiclePlate(driver.getVehiclePlate());
        vo.setVehicleType(driver.getVehicleType());
        vo.setVehicleCapacity(driver.getVehicleCapacity());
        vo.setFleetId(driver.getFleetId());
        vo.setStatus(driver.getStatus());
        vo.setHireDate(driver.getHireDate());
        vo.setCreatedAt(driver.getCreatedAt());
        vo.setUpdatedAt(driver.getUpdatedAt());
        return vo;
    }
}
