package com.renewable.ai.util;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DistributedLock {
    
    private static final Map<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Long> LOCK_TIME_MAP = new ConcurrentHashMap<>();
    private static final long LOCK_EXPIRE_MS = 30000;
    
    public boolean tryLock(String key, long timeoutMs) {
        ReentrantLock lock = LOCK_MAP.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            boolean acquired = lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
            if (acquired) {
                LOCK_TIME_MAP.put(key, System.currentTimeMillis());
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    public void unlock(String key) {
        ReentrantLock lock = LOCK_MAP.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            LOCK_TIME_MAP.remove(key);
        }
    }
    
    public boolean isLocked(String key) {
        ReentrantLock lock = LOCK_MAP.get(key);
        return lock != null && lock.isLocked();
    }
    
    public void cleanupExpiredLocks() {
        long now = System.currentTimeMillis();
        LOCK_TIME_MAP.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > LOCK_EXPIRE_MS) {
                ReentrantLock lock = LOCK_MAP.get(entry.getKey());
                if (lock != null && !lock.isLocked()) {
                    LOCK_MAP.remove(entry.getKey());
                    return true;
                }
            }
            return false;
        });
    }
    
    public static String getOrderLockKey(Long orderId) {
        return "order_lock:" + orderId;
    }
    
    public static String getDriverLockKey(Long driverId) {
        return "driver_lock:" + driverId;
    }
}
