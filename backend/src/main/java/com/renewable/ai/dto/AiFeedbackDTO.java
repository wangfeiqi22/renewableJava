package com.renewable.ai.dto;

public class AiFeedbackDTO {
    private String originalImageUrl;
    private String predictedType;
    private String userCorrectedType;
    private Integer confidence;

    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public void setOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
    }

    public String getPredictedType() {
        return predictedType;
    }

    public void setPredictedType(String predictedType) {
        this.predictedType = predictedType;
    }

    public String getUserCorrectedType() {
        return userCorrectedType;
    }

    public void setUserCorrectedType(String userCorrectedType) {
        this.userCorrectedType = userCorrectedType;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }
}
