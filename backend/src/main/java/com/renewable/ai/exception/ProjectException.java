package com.renewable.ai.exception;

public class ProjectException extends RuntimeException {
    
    private final String code;
    
    public ProjectException(String message) {
        super(message);
        this.code = "PROJECT_ERROR";
    }
    
    public ProjectException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static ProjectException projectNotFound() {
        return new ProjectException("PROJECT_NOT_FOUND", "项目不存在");
    }
    
    public static ProjectException projectNotFound(Long id) {
        return new ProjectException("PROJECT_NOT_FOUND", "项目不存在，ID: " + id);
    }
    
    public static ProjectException unauthorizedAccess() {
        return new ProjectException("UNAUTHORIZED_ACCESS", "无权操作此项目");
    }
    
    public static ProjectException duplicateProjectCode() {
        return new ProjectException("DUPLICATE_PROJECT_CODE", "项目编号已存在");
    }
    
    public static ProjectException invalidStatus(String currentStatus, String targetStatus) {
        return new ProjectException("INVALID_PROJECT_STATUS", 
                "项目当前状态为 " + currentStatus + "，无法转换为 " + targetStatus);
    }
    
    public static ProjectException hasActiveOrders() {
        return new ProjectException("HAS_ACTIVE_ORDERS", "项目有正在进行的订单，无法终止");
    }
    
    public static ProjectException contractExpired() {
        return new ProjectException("CONTRACT_EXPIRED", "项目合同已过期");
    }
}
