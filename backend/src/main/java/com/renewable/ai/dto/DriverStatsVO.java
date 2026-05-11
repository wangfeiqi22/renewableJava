package com.renewable.ai.dto;

import java.util.List;

public class DriverStatsVO {
    
    private Long driverId;
    private String driverName;
    private String phone;
    private String vehiclePlate;
    private String status;
    private long totalOrders;
    private long completedOrders;
    private long inProgressOrders;
    private double completionRate;
    private double averageOrderTime;
    private Double currentLatitude;
    private Double currentLongitude;
    private String lastLocationUpdate;
    private List<OrderVO> currentOrders;
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    
    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
    
    public long getInProgressOrders() { return inProgressOrders; }
    public void setInProgressOrders(long inProgressOrders) { this.inProgressOrders = inProgressOrders; }
    
    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    
    public double getAverageOrderTime() { return averageOrderTime; }
    public void setAverageOrderTime(double averageOrderTime) { this.averageOrderTime = averageOrderTime; }
    
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }
    
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }
    
    public String getLastLocationUpdate() { return lastLocationUpdate; }
    public void setLastLocationUpdate(String lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }
    
    public List<OrderVO> getCurrentOrders() { return currentOrders; }
    public void setCurrentOrders(List<OrderVO> currentOrders) { this.currentOrders = currentOrders; }
}
