package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.*;
import com.example.tourify_system_be.dto.response.LoginResponse;
import com.example.tourify_system_be.service.AuthService;
import com.example.tourify_system_be.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreateRequest userDTO) {
        if (userService.emailExists(userDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email đã tồn tại.");
        }

        if (userService.userNameExists(userDTO.getUserName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên đăng nhập đã tồn tại.");
        }

        try {
            boolean result = userService.sendRegistrationToken(userDTO);
            if (!result) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Không thể gửi email xác nhận. Vui lòng kiểm tra thông tin và thử lại.");
            }
            return ResponseEntity.ok("Vui lòng kiểm tra email để xác nhận đăng ký.");
        } catch (Exception e) {
            logger.error("Lỗi trong quá trình gửi email xác nhận đăng ký: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi gửi email xác nhận. Vui lòng thử lại sau.");
        }
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

    /**
     * API xác nhận token từ email và tạo tài khoản (từ link xác nhận trong email).
     */
    @GetMapping("/confirm")
    public ResponseEntity<String> confirmUser(@RequestParam("token") String token) {
        boolean confirmed = userService.confirmTokenAndCreateUser(token);
        if (confirmed) {
            return ResponseEntity.ok("Xác nhận thành công! Bạn có thể đăng nhập.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Xác nhận thất bại hoặc token hết hạn.");
        }
    }
}
