package com.renewable.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispatch_logs")
public class DispatchLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "order_number", length = 32)
    private String orderNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;
    
    @Column(name = "action_description", length = 500)
    private String actionDescription;
    
    @Column(name = "operator_id")
    private Long operatorId;
    
    @Column(name = "operator_name", length = 50)
    private String operatorName;
    
    @Column(name = "previous_driver_name", length = 50)
    private String previousDriverName;
    
    @Column(name = "new_driver_name", length = 50)
    private String newDriverName;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;
    
    @PrePersist
    protected void onCreate() {
        if (actionTime == null) {
            actionTime = LocalDateTime.now();
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    
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
    
    public enum ActionType {
        CREATE_ORDER,
        ASSIGN_ORDER,
        REASSIGN_ORDER,
        UPDATE_STATUS,
        CANCEL_ORDER,
        UPDATE_DRIVER_LOCATION,
        MANUAL_DISPATCH,
        AUTO_DISPATCH
    }
}
