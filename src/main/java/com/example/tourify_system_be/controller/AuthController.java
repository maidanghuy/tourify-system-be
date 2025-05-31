package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.AuthForgotPasswordRequest;
import com.example.tourify_system_be.dto.request.AuthResetPasswordRequest;
import com.example.tourify_system_be.dto.request.LoginRequest;
import com.example.tourify_system_be.dto.response.LoginResponse;
import com.example.tourify_system_be.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/forgot-password")
    public APIResponse<?> forgotPassword(@RequestBody AuthForgotPasswordRequest request) {
//        System.out.println(request.getEmail());
        authService.sendResetPasswordEmail(request.getEmail());
        return APIResponse.<Void>builder()
                .message("Password reset email sent successfully")
                .result(null)
                .build();
    }
    /*
    Sample JSON:
    {git checkout -b
        "email": "<EMAIL>"
    }
    */

    @PostMapping("/reset-password")
    public APIResponse<?> resetPassword(@RequestBody AuthResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmPassword());
        return APIResponse.<Void>builder()
                .message("Password reset successfully")
                .result(null)
                .build();
    }
    /*
    Sample JSON:
    {
        "token": "<PASSWORD>",
        "newPassword": "<PASSWORD>",
        "confirmPassword": "<PASSWORD>"
    }
    */

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        // Thông thường header Authorization: "Bearer eyJhbGciOi..."
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid Authorization header");
        }
        String token = authHeader.substring(7);
        boolean result = authService.logout(token);

        if (result) {
            return ResponseEntity.ok("Logout successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
