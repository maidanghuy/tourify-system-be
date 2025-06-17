package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.*;
import com.example.tourify_system_be.dto.response.*;
import com.example.tourify_system_be.exception.*;
import com.example.tourify_system_be.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {
    @Autowired
    private final AuthService authService;
    @Autowired
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
    public APIResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return
                APIResponse.<LoginResponse>builder()
                        .result(authService.login(request))
                        .build();
    }
    /*
    Sample JSON:
    {
        "username": "johndoe",
        "password": "<PASSWORD>"
    }
    */

    @PostMapping("/register")
    public APIResponse<?> registerUser(@Valid @RequestBody UserCreateRequest userDTO) {
        if (userService.emailExists(userDTO.getEmail())) {
            throw new AppException(ErrorCode.
                    EMAIL_EXISTED);
        }

        if (userService.userNameExists(userDTO.getUserName())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        try {
            boolean result = userService.sendRegistrationToken(userDTO);
            if (!result) {
                throw new AppException(ErrorCode.FAIL_TO_SEND_REGISTRATION_TOKEN);
            }
            return APIResponse.<Void>builder()
                    .message("Registration email sent successfully")
                    .result(null)
                    .build();
        } catch (Exception e) {
            logger.error("Lỗi trong quá trình gửi email xác nhận đăng ký: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FAIL_TO_SEND_REGISTRATION_TOKEN);
        }
    }
    /*
    Sample JSON:
    {
        "email": "<EMAIL>",
        "password": "<PASSWORD>",
        "passwordConfirm": "<PASSWORD>"
    }
    */

    @PostMapping("/logout")
    public APIResponse<?> logout(@RequestHeader("Authorization") String authHeader) {
        // Thông thường header Authorization: "Bearer eyJhbGciOi..."
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        String token = authHeader.substring(7);
        boolean result = authService.logout(token);

        if (result) {
            return APIResponse.<Void>builder()
                            .message("Logout successfully")
                            .result(null)
                            .build();
        } else {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }


    /**
     * API xác nhận token từ email và tạo tài khoản (từ link xác nhận trong email).
     */
    @GetMapping("/confirm")
    public RedirectView confirmEmail(@RequestParam("token") String token) {
        boolean success = userService.confirmTokenAndCreateUser(token);
        if (success) {
            // Nếu thành công, chuyển hướng tới trang đăng nhập front-end
            String loginUrl = UriComponentsBuilder
                    .fromUriString("http://localhost:8080/tourify/landing")
                    .queryParam("info", "account_activated")
                    .build().toUriString();

            return new RedirectView(loginUrl);
        } else {
            // Token không hợp lệ hoặc lỗi -> quay lại trang đăng ký với thông báo lỗi
            return new RedirectView("http://localhost:8080/tourify/register?error=token_invalid");
        }
    }
    /*
    Sample JSON:
    {
        "token": "<TOKEN>"
    }
    */
}
