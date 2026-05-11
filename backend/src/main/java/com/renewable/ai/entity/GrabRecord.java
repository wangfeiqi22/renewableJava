package com.renewable.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grab_records", indexes = {
    @Index(name = "idx_grab_order_id", columnList = "order_id"),
    @Index(name = "idx_grab_driver_id", columnList = "driver_id"),
    @Index(name = "idx_grab_time", columnList = "grab_time")
})
public class GrabRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "order_number", length = 32)
    private String orderNumber;
    
    @Column(name = "driver_id", nullable = false)
    private Long driverId;
    
    @Column(name = "driver_name", length = 50)
    private String driverName;
    
    @Column(name = "fleet_id", nullable = false)
    private Long fleetId;
    
    @Column(name = "grab_time", nullable = false)
    private LocalDateTime grabTime;
    
    @Column(name = "success", nullable = false)
    private Boolean success;
    
    @Column(name = "fail_reason", length = 200)
    private String failReason;
    
    @Column(name = "position", nullable = false)
    private Integer position;
    
    @Column(name = "distance_km", precision = 8)
    private Double distanceKm;
    
    @Column(name = "is_winner", nullable = false)
    private Boolean isWinner;
    
    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;
    
    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;
    
    @PrePersist
    protected void onCreate() {
        if (grabTime == null) {
            grabTime = LocalDateTime.now();
        }
        if (success == null) {
            success = false;
        }
        if (isWinner == null) {
            isWinner = false;
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public Long getFleetId() { return fleetId; }
    public void setFleetId(Long fleetId) { this.fleetId = fleetId; }
    
    public LocalDateTime getGrabTime() { return grabTime; }
    public void setGrabTime(LocalDateTime grabTime) { this.grabTime = grabTime; }
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    
    public Boolean getIsWinner() { return isWinner; }
    public void setIsWinner(Boolean isWinner) { this.isWinner = isWinner; }
    
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
    
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
}
