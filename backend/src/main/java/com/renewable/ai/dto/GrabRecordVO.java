package com.renewable.ai.dto;

import com.renewable.ai.entity.GrabRecord;
import java.time.LocalDateTime;

public class GrabRecordVO {
    
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long driverId;
    private String driverName;
    private Long fleetId;
    private LocalDateTime grabTime;
    private Boolean success;
    private String failReason;
    private Integer position;
    private Double distanceKm;
    private Boolean isWinner;
    private LocalDateTime cancelTime;
    private String cancelReason;
    
    public static GrabRecordVO fromEntity(GrabRecord record) {
        GrabRecordVO vo = new GrabRecordVO();
        vo.setId(record.getId());
        vo.setOrderId(record.getOrderId());
        vo.setOrderNumber(record.getOrderNumber());
        vo.setDriverId(record.getDriverId());
        vo.setDriverName(record.getDriverName());
        vo.setFleetId(record.getFleetId());
        vo.setGrabTime(record.getGrabTime());
        vo.setSuccess(record.getSuccess());
        vo.setFailReason(record.getFailReason());
        vo.setPosition(record.getPosition());
        vo.setDistanceKm(record.getDistanceKm());
        vo.setIsWinner(record.getIsWinner());
        vo.setCancelTime(record.getCancelTime());
        vo.setCancelReason(record.getCancelReason());
        return vo;
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
