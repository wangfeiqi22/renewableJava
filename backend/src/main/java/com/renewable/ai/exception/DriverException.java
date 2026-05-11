package com.renewable.ai.exception;

public class DriverException extends RuntimeException {
    
    private final int code;
    
    public DriverException(String message) {
        super(message);
        this.code = 400;
    }
    
    public DriverException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public static DriverException phoneAlreadyExists(String phone) {
        return new DriverException(400, "手机号 " + phone + " 已被使用");
    }
    
    public static DriverException invalidLicenseFormat() {
        return new DriverException(400, "驾驶证号格式不正确");
    }
    
    public static DriverException driverNotFound(Long id) {
        return new DriverException(404, "司机不存在: " + id);
    }
    
    public static DriverException hasPendingOrders() {
        return new DriverException(400, "该司机存在未完成的清运单，无法删除");
    }
    
    public static DriverException unauthorizedAccess() {
        return new DriverException(403, "无权访问该司机信息");
    }
}
