package com.example.tourify_system_be.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenUtils {
    private String generateSecureToken(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[byteLength];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
