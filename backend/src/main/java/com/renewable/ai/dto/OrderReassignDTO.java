package com.renewable.ai.dto;

import jakarta.validation.constraints.NotNull;

public class OrderReassignDTO {
    
    @NotNull(message = "新司机ID不能为空")
    private Long newDriverId;
    
    private String reason;
    
    public Long getNewDriverId() { return newDriverId; }
    public void setNewDriverId(Long newDriverId) { this.newDriverId = newDriverId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
