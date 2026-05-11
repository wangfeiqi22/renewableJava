package com.renewable.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grab_rules")
public class GrabRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fleet_id", nullable = false)
    private Long fleetId;
    
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
    
    @Column(name = "max_orders_per_driver_per_day")
    private Integer maxOrdersPerDriverPerDay;
    
    @Column(name = "min_interval_seconds")
    private Integer minIntervalSeconds;
    
    @Column(name = "max_distance_km", precision = 8)
    private Double maxDistanceKm;
    
    @Column(name = "priority_enabled")
    private Boolean priorityEnabled;
    
    @Column(name = "first_come_first_serve", nullable = false)
    private Boolean firstComeFirstServe;
    
    @Column(name = "allow_multiple_grab")
    private Boolean allowMultipleGrab;
    
    @Column(name = "auto_assign", nullable = false)
    private Boolean autoAssign;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "operator_id")
    private Long operatorId;
    
    @Column(name = "operator_name", length = 50)
    private String operatorName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enabled == null) enabled = true;
        if (firstComeFirstServe == null) firstComeFirstServe = true;
        if (autoAssign == null) autoAssign = false;
        if (priorityEnabled == null) priorityEnabled = true;
        if (allowMultipleGrab == null) allowMultipleGrab = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFleetId() { return fleetId; }
    public void setFleetId(Long fleetId) { this.fleetId = fleetId; }
    
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public Integer getMaxOrdersPerDriverPerDay() { return maxOrdersPerDriverPerDay; }
    public void setMaxOrdersPerDriverPerDay(Integer maxOrdersPerDriverPerDay) { this.maxOrdersPerDriverPerDay = maxOrdersPerDriverPerDay; }
    
    public Integer getMinIntervalSeconds() { return minIntervalSeconds; }
    public void setMinIntervalSeconds(Integer minIntervalSeconds) { this.minIntervalSeconds = minIntervalSeconds; }
    
    public Double getMaxDistanceKm() { return maxDistanceKm; }
    public void setMaxDistanceKm(Double maxDistanceKm) { this.maxDistanceKm = maxDistanceKm; }
    
    public Boolean getPriorityEnabled() { return priorityEnabled; }
    public void setPriorityEnabled(Boolean priorityEnabled) { this.priorityEnabled = priorityEnabled; }
    
    public Boolean getFirstComeFirstServe() { return firstComeFirstServe; }
    public void setFirstComeFirstServe(Boolean firstComeFirstServe) { this.firstComeFirstServe = firstComeFirstServe; }
    
    public Boolean getAllowMultipleGrab() { return allowMultipleGrab; }
    public void setAllowMultipleGrab(Boolean allowMultipleGrab) { this.allowMultipleGrab = allowMultipleGrab; }
    
    public Boolean getAutoAssign() { return autoAssign; }
    public void setAutoAssign(Boolean autoAssign) { this.autoAssign = autoAssign; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
