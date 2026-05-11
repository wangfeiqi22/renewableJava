package com.renewable.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GrabRuleDTO {
    
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称不能超过100字符")
    private String ruleName;
    
    private Boolean enabled = true;
    
    private Integer maxOrdersPerDriverPerDay;
    
    private Integer minIntervalSeconds;
    
    private Double maxDistanceKm;
    
    private Boolean priorityEnabled = true;
    
    private Boolean firstComeFirstServe = true;
    
    private Boolean allowMultipleGrab = false;
    
    private Boolean autoAssign = false;
    
    @Size(max = 500, message = "描述不能超过500字符")
    private String description;
    
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
}
