package com.renewable.ai.exception;

public class GrabException extends RuntimeException {
    
    private final String code;
    
    public GrabException(String message) {
        super(message);
        this.code = "GRAB_ERROR";
    }
    
    public GrabException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static GrabException orderNotInPool() {
        return new GrabException("ORDER_NOT_IN_POOL", "订单不在抢单池中");
    }
    
    public static GrabException alreadyGrabbed() {
        return new GrabException("ALREADY_GRABBED", "订单已被抢");
    }
    
    public static GrabException driverNotAvailable() {
        return new GrabException("DRIVER_NOT_AVAILABLE", "司机不可用");
    }
    
    public static GrabException maxOrdersReached() {
        return new GrabException("MAX_ORDERS_REACHED", "今日抢单次数已达上限");
    }
    
    public static GrabException tooFrequent() {
        return new GrabException("TOO_FREQUENT", "抢单过于频繁，请稍后再试");
    }
    
    public static GrabException distanceTooFar() {
        return new GrabException("DISTANCE_TOO_FAR", "距离太远，无法抢单");
    }
    
    public static GrabException alreadyParticipated() {
        return new GrabException("ALREADY_PARTICIPATED", "您已参与过此订单的抢单");
    }
    
    public static GrabException lockFailed() {
        return new GrabException("LOCK_FAILED", "系统繁忙，请稍后重试");
    }
    
    public static GrabException ruleNotFound() {
        return new GrabException("RULE_NOT_FOUND", "抢单规则不存在");
    }
}
