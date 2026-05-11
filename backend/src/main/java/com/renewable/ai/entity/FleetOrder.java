package com.renewable.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_orders", indexes = {
    @Index(name = "idx_fleet_order_fleet_id", columnList = "fleet_id"),
    @Index(name = "idx_fleet_order_driver_id", columnList = "driver_id"),
    @Index(name = "idx_fleet_order_status", columnList = "status"),
    @Index(name = "idx_fleet_order_create_time", columnList = "created_at")
})
public class FleetOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;
    
    @Column(name = "fleet_id", nullable = false)
    private Long fleetId;
    
    @Column(name = "driver_id")
    private Long driverId;
    
    @Column(name = "driver_name", length = 50)
    private String driverName;
    
    @Column(name = "driver_phone", length = 20)
    private String driverPhone;
    
    @Column(name = "project_id")
    private Long projectId;
    
    @Column(name = "project_name", length = 100)
    private String projectName;
    
    @Column(name = "waste_type", length = 50)
    private String wasteType;
    
    @Column(name = "estimated_weight", precision = 10)
    private Double estimatedWeight;
    
    @Column(name = "actual_weight", precision = 10)
    private Double actualWeight;
    
    @Column(name = "pickup_address", length = 200)
    private String pickupAddress;
    
    @Column(name = "pickup_latitude", precision = 10)
    private Double pickupLatitude;
    
    @Column(name = "pickup_longitude", precision = 10)
    private Double pickupLongitude;
    
    @Column(name = "destination_address", length = 200)
    private String destinationAddress;
    
    @Column(name = "destination_latitude", precision = 10)
    private Double destinationLatitude;
    
    @Column(name = "destination_longitude", precision = 10)
    private Double destinationLongitude;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    private OrderPriority priority;
    
    @Column(name = "scheduled_pickup_time")
    private LocalDateTime scheduledPickupTime;
    
    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;
    
    @Column(name = "completed_time")
    private LocalDateTime completedTime;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "operator_id")
    private Long operatorId;
    
    @Column(name = "operator_name", length = 50)
    private String operatorName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_pooled", nullable = false)
    private Boolean isPooled = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }
        if (status == null) {
            status = OrderStatus.PENDING_ASSIGNMENT;
        }
        if (isPooled == null) {
            isPooled = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public Long getFleetId() { return fleetId; }
    public void setFleetId(Long fleetId) { this.fleetId = fleetId; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }
    
    public Double getEstimatedWeight() { return estimatedWeight; }
    public void setEstimatedWeight(Double estimatedWeight) { this.estimatedWeight = estimatedWeight; }
    
    public Double getActualWeight() { return actualWeight; }
    public void setActualWeight(Double actualWeight) { this.actualWeight = actualWeight; }
    
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
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public OrderPriority getPriority() { return priority; }
    public void setPriority(OrderPriority priority) { this.priority = priority; }
    
    public LocalDateTime getScheduledPickupTime() { return scheduledPickupTime; }
    public void setScheduledPickupTime(LocalDateTime scheduledPickupTime) { this.scheduledPickupTime = scheduledPickupTime; }
    
    public LocalDateTime getActualPickupTime() { return actualPickupTime; }
    public void setActualPickupTime(LocalDateTime actualPickupTime) { this.actualPickupTime = actualPickupTime; }
    
    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Boolean getIsPooled() { return isPooled; }
    public void setIsPooled(Boolean isPooled) { this.isPooled = isPooled; }
    
    public enum OrderStatus {
        PENDING_ASSIGNMENT,
        ASSIGNED,
        PICKED_UP,
        IN_TRANSIT,
        ARRIVED,
        UNLOADING,
        COMPLETED,
        CANCELLED
    }
    
    public enum OrderPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
