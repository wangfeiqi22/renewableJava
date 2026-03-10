package com.renewable.ai.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityUtil {

    private static final ConcurrentHashMap<String, Long> TOKEN_USER_MAP = new ConcurrentHashMap<>();

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
}
