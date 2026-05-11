package com.renewable.ai.dto;

public class AiRecognizeResponse {
    private String itemName;
    private String category;
    private Integer confidence;
    private String advice;
    private String environmentalTips;
    private String abTestGroup;
    private Double estimatedVolume;
    private String volumeUnit;
    private Integer estimatedCount;
    private String estimatedWeight;
    private String smartRemark;
    private String disposalMethod;
    private String recyclingValue;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getEnvironmentalTips() {
        return environmentalTips;
    }

    public void setEnvironmentalTips(String environmentalTips) {
        this.environmentalTips = environmentalTips;
    }

    public String getAbTestGroup() {
        return abTestGroup;
    }

    public void setAbTestGroup(String abTestGroup) {
        this.abTestGroup = abTestGroup;
    }

    public Double getEstimatedVolume() {
        return estimatedVolume;
    }

    public void setEstimatedVolume(Double estimatedVolume) {
        this.estimatedVolume = estimatedVolume;
    }

    public String getVolumeUnit() {
        return volumeUnit;
    }

    public void setVolumeUnit(String volumeUnit) {
        this.volumeUnit = volumeUnit;
    }

    public Integer getEstimatedCount() {
        return estimatedCount;
    }

    public void setEstimatedCount(Integer estimatedCount) {
        this.estimatedCount = estimatedCount;
    }

    public String getEstimatedWeight() {
        return estimatedWeight;
    }

    public void setEstimatedWeight(String estimatedWeight) {
        this.estimatedWeight = estimatedWeight;
    }

    public String getSmartRemark() {
        return smartRemark;
    }

    public void setSmartRemark(String smartRemark) {
        this.smartRemark = smartRemark;
    }

    public String getDisposalMethod() {
        return disposalMethod;
    }

    public void setDisposalMethod(String disposalMethod) {
        this.disposalMethod = disposalMethod;
    }

    public String getRecyclingValue() {
        return recyclingValue;
    }

    public void setRecyclingValue(String recyclingValue) {
        this.recyclingValue = recyclingValue;
    }
}
