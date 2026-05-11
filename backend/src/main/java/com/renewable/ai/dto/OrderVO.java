package com.renewable.ai.dto;

import com.renewable.ai.entity.FleetOrder;
import java.time.LocalDateTime;

public class OrderVO {
    
    private Long id;
    private String orderNumber;
    private Long fleetId;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private Long projectId;
    private String projectName;
    private String wasteType;
    private Double estimatedWeight;
    private Double actualWeight;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String status;
    private String statusDescription;
    private String priority;
    private LocalDateTime scheduledPickupTime;
    private LocalDateTime actualPickupTime;
    private LocalDateTime completedTime;
    private String notes;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPooled;
    
    public static OrderVO fromEntity(FleetOrder order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNumber(order.getOrderNumber());
        vo.setFleetId(order.getFleetId());
        vo.setDriverId(order.getDriverId());
        vo.setDriverName(order.getDriverName());
        vo.setDriverPhone(order.getDriverPhone());
        vo.setProjectId(order.getProjectId());
        vo.setProjectName(order.getProjectName());
        vo.setWasteType(order.getWasteType());
        vo.setEstimatedWeight(order.getEstimatedWeight());
        vo.setActualWeight(order.getActualWeight());
        vo.setPickupAddress(order.getPickupAddress());
        vo.setPickupLatitude(order.getPickupLatitude());
        vo.setPickupLongitude(order.getPickupLongitude());
        vo.setDestinationAddress(order.getDestinationAddress());
        vo.setDestinationLatitude(order.getDestinationLatitude());
        vo.setDestinationLongitude(order.getDestinationLongitude());
        vo.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        vo.setStatusDescription(getStatusDescription(order.getStatus()));
        vo.setPriority(order.getPriority() != null ? order.getPriority().name() : null);
        vo.setScheduledPickupTime(order.getScheduledPickupTime());
        vo.setActualPickupTime(order.getActualPickupTime());
        vo.setCompletedTime(order.getCompletedTime());
        vo.setNotes(order.getNotes());
        vo.setOperatorId(order.getOperatorId());
        vo.setOperatorName(order.getOperatorName());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());
        vo.setIsPooled(order.getIsPooled());
        return vo;
    }
    
    private static String getStatusDescription(FleetOrder.OrderStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING_ASSIGNMENT: return "待指派";
            case ASSIGNED: return "已指派";
            case PICKED_UP: return "已取货";
            case IN_TRANSIT: return "运输中";
            case ARRIVED: return "已到达";
            case UNLOADING: return "卸货中";
            case COMPLETED: return "已完成";
            case CANCELLED: return "已取消";
            default: return status.name();
        }
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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
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
}
