package com.renewable.ai.dto;

import java.util.List;

public class DashboardVO {
    
    private long totalOrders;
    private long pendingOrders;
    private long inProgressOrders;
    private long completedOrders;
    private long cancelledOrders;
    private double completionRate;
    private double averageProcessingTime;
    private long totalDrivers;
    private long activeDrivers;
    private double driverUtilizationRate;
    private List<DailyStatsVO> dailyStats;
    
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    
    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
    
    public long getInProgressOrders() { return inProgressOrders; }
    public void setInProgressOrders(long inProgressOrders) { this.inProgressOrders = inProgressOrders; }
    
    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
    
    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    
    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    
    public double getAverageProcessingTime() { return averageProcessingTime; }
    public void setAverageProcessingTime(double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }
    
    public long getTotalDrivers() { return totalDrivers; }
    public void setTotalDrivers(long totalDrivers) { this.totalDrivers = totalDrivers; }
    
    public long getActiveDrivers() { return activeDrivers; }
    public void setActiveDrivers(long activeDrivers) { this.activeDrivers = activeDrivers; }
    
    public double getDriverUtilizationRate() { return driverUtilizationRate; }
    public void setDriverUtilizationRate(double driverUtilizationRate) { this.driverUtilizationRate = driverUtilizationRate; }
    
    public List<DailyStatsVO> getDailyStats() { return dailyStats; }
    public void setDailyStats(List<DailyStatsVO> dailyStats) { this.dailyStats = dailyStats; }
    
    public static class DailyStatsVO {
        private String date;
        private long createdOrders;
        private long completedOrders;
        private double totalWeight;
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public long getCreatedOrders() { return createdOrders; }
        public void setCreatedOrders(long createdOrders) { this.createdOrders = createdOrders; }
        
        public long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
        
        public double getTotalWeight() { return totalWeight; }
        public void setTotalWeight(double totalWeight) { this.totalWeight = totalWeight; }
    }
}
