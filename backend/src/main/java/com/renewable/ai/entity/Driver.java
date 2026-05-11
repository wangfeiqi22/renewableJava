package com.renewable.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fleet_drivers")
public class Driver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String phone;
    
    @Column(name = "id_number")
    private String idNumber;
    
    @Column(name = "driver_license_number")
    private String driverLicenseNumber;
    
    @Column(name = "driver_license_level")
    private String driverLicenseLevel;
    
    @Column(name = "vehicle_plate")
    private String vehiclePlate;
    
    @Column(name = "vehicle_type")
    private String vehicleType;
    
    @Column(name = "vehicle_capacity")
    private BigDecimal vehicleCapacity;
    
    @Column(name = "fleet_id", nullable = false)
    private Long fleetId;
    
    @Column(length = 20)
    private String status;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private Double currentLatitude;
    
    private Double currentLongitude;
    
    private LocalDateTime lastLocationUpdate;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getIdNumber() {
        return idNumber;
    }
    
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
    
    public String getDriverLicenseNumber() {
        return driverLicenseNumber;
    }
    
    public void setDriverLicenseNumber(String driverLicenseNumber) {
        this.driverLicenseNumber = driverLicenseNumber;
    }
    
    public String getDriverLicenseLevel() {
        return driverLicenseLevel;
    }
    
    public void setDriverLicenseLevel(String driverLicenseLevel) {
        this.driverLicenseLevel = driverLicenseLevel;
    }
    
    public String getVehiclePlate() {
        return vehiclePlate;
    }
    
    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public BigDecimal getVehicleCapacity() {
        return vehicleCapacity;
    }
    
    public void setVehicleCapacity(BigDecimal vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }
    
    public Long getFleetId() {
        return fleetId;
    }
    
    public void setFleetId(Long fleetId) {
        this.fleetId = fleetId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }
    
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }
    
    public LocalDateTime getLastLocationUpdate() { return lastLocationUpdate; }
    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "active";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
