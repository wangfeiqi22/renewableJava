package com.renewable.ai.dto;

import java.time.LocalDateTime;

public class DispatchLogVO {
    
    private Long id;
    private String orderNumber;
    private Long orderId;
    private String actionType;
    private String actionDescription;
    private Long operatorId;
    private String operatorName;
    private String previousDriverName;
    private String newDriverName;
    private String reason;
    private LocalDateTime actionTime;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }
    
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public String getPreviousDriverName() { return previousDriverName; }
    public void setPreviousDriverName(String previousDriverName) { this.previousDriverName = previousDriverName; }
    
    public String getNewDriverName() { return newDriverName; }
    public void setNewDriverName(String newDriverName) { this.newDriverName = newDriverName; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
}
