package com.renewable.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class OrderAssignDTO {
    
    @NotEmpty(message = "订单ID列表不能为空")
    private List<Long> orderIds;
    
    @NotEmpty(message = "司机ID不能为空")
    private Long driverId;
    
    private String notes;
    
    public List<Long> getOrderIds() { return orderIds; }
    public void setOrderIds(List<Long> orderIds) { this.orderIds = orderIds; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
