package com.renewable.ai.dto;

import com.renewable.ai.entity.GrabRule;
import java.time.LocalDateTime;

public class GrabRuleVO {
    
    private Long id;
    private Long fleetId;
    private String ruleName;
    private Boolean enabled;
    private Integer maxOrdersPerDriverPerDay;
    private Integer minIntervalSeconds;
    private Double maxDistanceKm;
    private Boolean priorityEnabled;
    private Boolean firstComeFirstServe;
    private Boolean allowMultipleGrab;
    private Boolean autoAssign;
    private String description;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static GrabRuleVO fromEntity(GrabRule rule) {
        GrabRuleVO vo = new GrabRuleVO();
        vo.setId(rule.getId());
        vo.setFleetId(rule.getFleetId());
        vo.setRuleName(rule.getRuleName());
        vo.setEnabled(rule.getEnabled());
        vo.setMaxOrdersPerDriverPerDay(rule.getMaxOrdersPerDriverPerDay());
        vo.setMinIntervalSeconds(rule.getMinIntervalSeconds());
        vo.setMaxDistanceKm(rule.getMaxDistanceKm());
        vo.setPriorityEnabled(rule.getPriorityEnabled());
        vo.setFirstComeFirstServe(rule.getFirstComeFirstServe());
        vo.setAllowMultipleGrab(rule.getAllowMultipleGrab());
        vo.setAutoAssign(rule.getAutoAssign());
        vo.setDescription(rule.getDescription());
        vo.setOperatorId(rule.getOperatorId());
        vo.setOperatorName(rule.getOperatorName());
        vo.setCreatedAt(rule.getCreatedAt());
        vo.setUpdatedAt(rule.getUpdatedAt());
        return vo;
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
