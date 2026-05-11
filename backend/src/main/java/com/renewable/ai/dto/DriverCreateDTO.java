package com.renewable.ai.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

public class DriverCreateDTO {
    private String name;
    private String phone;
    private String idNumber;
    private String driverLicenseNumber;
    private String driverLicenseLevel;
    private String vehiclePlate;
    private String vehicleType;
    private BigDecimal vehicleCapacity;
    private LocalDate hireDate;
    
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
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
