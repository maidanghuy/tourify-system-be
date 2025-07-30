package com.example.tourify_system_be.utils;

import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.security.CustomUserDetails;
import com.example.tourify_system_be.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Validate JWT token and check if user has SUB_COMPANY role
     * 
     * @param token The JWT token from Authorization header
     * @return CustomUserDetails if validation passes
     * @throws AppException if token is invalid or user doesn't have required role
     */
    public CustomUserDetails validateSubCompanyToken(String token) {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid token");
        }

        // Extract user details from token
        CustomUserDetails userDetails = jwtUtil.getUserDetailsFromToken(token);

        // Check if user has SUB_COMPANY role
        if (!"SUB_COMPANY".equals(userDetails.getRole())) {
            throw new AppException(ErrorCode.ROLE_NOT_ALLOWED,
                    "Only SUB_COMPANY role is allowed to perform this operation");
        }

        return userDetails;
    }

    /**
     * Validate JWT token and check if user has ADMIN role
     * 
     * @param token The JWT token from Authorization header
     * @return CustomUserDetails if validation passes
     * @throws AppException if token is invalid or user doesn't have required role
     */
    public CustomUserDetails validateAdminToken(String token) {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid token");
        }

        // Extract user details from token
        CustomUserDetails userDetails = jwtUtil.getUserDetailsFromToken(token);

        // Check if user has ADMIN role
        if (!"ADMIN".equals(userDetails.getRole())) {
            throw new AppException(ErrorCode.ROLE_NOT_ALLOWED, "Only ADMIN role is allowed to perform this operation");
        }

        return userDetails;
    }

    /**
     * Validate JWT token and check if user has any of the specified roles
     * 
     * @param token        The JWT token from Authorization header
     * @param allowedRoles Array of allowed roles
     * @return CustomUserDetails if validation passes
     * @throws AppException if token is invalid or user doesn't have required role
     */
    public CustomUserDetails validateTokenWithRoles(String token, String... allowedRoles) {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid token");
        }

        // Extract user details from token
        CustomUserDetails userDetails = jwtUtil.getUserDetailsFromToken(token);

        // Check if user has any of the allowed roles
        boolean hasAllowedRole = false;
        for (String role : allowedRoles) {
            if (role.equals(userDetails.getRole())) {
                hasAllowedRole = true;
                break;
            }
        }

        if (!hasAllowedRole) {
            throw new AppException(ErrorCode.ROLE_NOT_ALLOWED, "User role not allowed for this operation");
        }

        return userDetails;
    }

    /**
     * Extract user ID from JWT token without role validation
     * 
     * @param token The JWT token from Authorization header
     * @return User ID if token is valid
     * @throws AppException if token is invalid
     */
    public String extractUserId(String token) {
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid token");
        }

        // Extract user details from token
        CustomUserDetails userDetails = jwtUtil.getUserDetailsFromToken(token);
        return userDetails.getId();
    }
}