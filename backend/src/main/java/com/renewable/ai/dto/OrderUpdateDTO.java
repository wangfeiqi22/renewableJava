package com.renewable.ai.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class OrderUpdateDTO {
    
    private Long projectId;
    
    private String projectName;
    
    @Size(max = 50, message = "废物类型不能超过50字符")
    private String wasteType;
    
    @DecimalMin(value = "0.01", message = "预估重量必须大于0")
    @DecimalMax(value = "99999.99", message = "预估重量不能超过99999.99")
    private Double estimatedWeight;
    
    @Size(max = 200, message = "取货地址不能超过200字符")
    private String pickupAddress;
    
    private Double pickupLatitude;
    
    private Double pickupLongitude;
    
    @Size(max = 200, message = "目的地址不能超过200字符")
    private String destinationAddress;
    
    private Double destinationLatitude;
    
    private Double destinationLongitude;
    
    private String priority;
    
    private LocalDateTime scheduledPickupTime;
    
    @Size(max = 500, message = "备注不能超过500字符")
    private String notes;
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }
    
    public Double getEstimatedWeight() { return estimatedWeight; }
    public void setEstimatedWeight(Double estimatedWeight) { this.estimatedWeight = estimatedWeight; }
    
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    
    public Double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(Double pickupLatitude) { this.pickupLatitude = pickupLatitude; }
    
    public Double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(Double pickupLongitude) { this.pickupLongitude = pickupLongitude; }
    
    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
    
    public Double getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(Double destinationLatitude) { this.destinationLatitude = destinationLatitude; }
    
    public Double getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(Double destinationLongitude) { this.destinationLongitude = destinationLongitude; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public LocalDateTime getScheduledPickupTime() { return scheduledPickupTime; }
    public void setScheduledPickupTime(LocalDateTime scheduledPickupTime) { this.scheduledPickupTime = scheduledPickupTime; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
