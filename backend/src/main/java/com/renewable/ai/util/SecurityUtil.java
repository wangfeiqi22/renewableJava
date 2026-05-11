package com.renewable.ai.util;

import com.renewable.ai.entity.User;
import com.renewable.ai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SecurityUtil {

    private static final ConcurrentHashMap<String, Long> TOKEN_USER_MAP = new ConcurrentHashMap<>();
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();
    private static UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository repository) {
        SecurityUtil.userRepository = repository;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void bindTokenToUser(String token, Long userId) {
        if (token == null || token.isBlank() || userId == null) return;
        TOKEN_USER_MAP.put(token, userId);
    }

    public static Long getUserIdByToken(String token) {
        if (token == null || token.isBlank()) return null;
        return TOKEN_USER_MAP.get(token);
    }

    public static void revokeToken(String token) {
        if (token == null || token.isBlank()) return;
        TOKEN_USER_MAP.remove(token);
    }

    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashPassword(rawPassword).equals(hashedPassword);
    }
    
    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }
    
    public static void setCurrentUser(User user) {
        CURRENT_USER.set(user);
    }
    
    public static void clearCurrentUser() {
        CURRENT_USER.remove();
    }
    
    public static User getUserByToken(String token) {
        Long userId = getUserIdByToken(token);
        if (userId != null && userRepository != null) {
            return userRepository.findById(userId).orElse(null);
        }
        return null;
    }
}
