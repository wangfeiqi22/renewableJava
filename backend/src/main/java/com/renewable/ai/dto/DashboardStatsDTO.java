package com.renewable.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

public class DashboardStatsDTO {
    private Long totalOrders;
    private Long completedOrders;
    private Long pendingOrders;
    private Double completionRate;
    
    public DashboardStatsDTO() {}
    
    public DashboardStatsDTO(Long totalOrders, Long completedOrders, Long pendingOrders, Double completionRate) {
        this.totalOrders = totalOrders;
        this.completedOrders = completedOrders;
        this.pendingOrders = pendingOrders;
        this.completionRate = completionRate;
    }
    
    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
    
    public Long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(Long completedOrders) { this.completedOrders = completedOrders; }
    
    public Long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(Long pendingOrders) { this.pendingOrders = pendingOrders; }
    
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
}
