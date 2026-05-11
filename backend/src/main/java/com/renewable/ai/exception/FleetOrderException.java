package com.renewable.ai.exception;

public class FleetOrderException extends RuntimeException {
    
    private final String code;
    
    public FleetOrderException(String message) {
        super(message);
        this.code = "FLEET_ORDER_ERROR";
    }
    
    public FleetOrderException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static FleetOrderException orderNotFound() {
        return new FleetOrderException("ORDER_NOT_FOUND", "订单不存在");
    }
    
    public static FleetOrderException orderNotFound(Long id) {
        return new FleetOrderException("ORDER_NOT_FOUND", "订单不存在，ID: " + id);
    }
    
    public static FleetOrderException invalidStatus(String currentStatus, String targetStatus) {
        return new FleetOrderException("INVALID_ORDER_STATUS", 
                "订单当前状态为 " + currentStatus + "，无法转换为 " + targetStatus);
    }
    
    public static FleetOrderException unauthorizedAccess() {
        return new FleetOrderException("UNAUTHORIZED_ACCESS", "无权操作此订单");
    }
    
    public static FleetOrderException driverNotAvailable() {
        return new FleetOrderException("DRIVER_NOT_AVAILABLE", "司机不可用或已被其他订单占用");
    }
    
    public static FleetOrderException driverNotFound() {
        return new FleetOrderException("DRIVER_NOT_FOUND", "司机不存在");
    }
    
    public static FleetOrderException driverHasActiveOrders() {
        return new FleetOrderException("DRIVER_HAS_ACTIVE_ORDERS", "司机有正在进行的订单，无法指派");
    }
    
    public static FleetOrderException invalidOrderOperation(String message) {
        return new FleetOrderException("INVALID_ORDER_OPERATION", message);
    }
    
    public static FleetOrderException projectNotFound() {
        return new FleetOrderException("PROJECT_NOT_FOUND", "项目不存在");
    }
    
    public static FleetOrderException duplicateOrderNumber() {
        return new FleetOrderException("DUPLICATE_ORDER_NUMBER", "订单号重复");
    }
}
