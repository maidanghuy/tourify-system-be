package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.*;
import com.example.tourify_system_be.dto.response.*;
import com.example.tourify_system_be.exception.*;
import com.example.tourify_system_be.repository.IUserRepository;
import com.example.tourify_system_be.security.JwtUtil;
import com.example.tourify_system_be.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {
    @Autowired
    private final AuthService authService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtUtil jwtUtil;
    @Autowired
    private final IUserRepository userRepository;
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
    public APIResponse<?> register(@Valid @RequestBody UserCreateRequest userDTO) {
        if (userService.emailExists(userDTO.getEmail())) {
            throw new AppException(ErrorCode.
                    EMAIL_EXISTED, "Email already exists!");

        }

        if (userService.userNameExists(userDTO.getUserName())) {
            throw new AppException(ErrorCode.USER_EXISTED, "User already exists!");
        }

        try {
            boolean result = userService.sendRegistrationToken(userDTO);
            if (!result) {
                throw new AppException(ErrorCode.FAIL_TO_SEND_REGISTRATION_TOKEN, "Unable to send registration token. Please try again later!");
            }
            return APIResponse.<Void>builder()
                    .message("Registration email sent successfully")
                    .result(null)
                    .build();
        } catch (Exception e) {
            logger.error("Failed to send registration confirmation email: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FAIL_TO_SEND_REGISTRATION_TOKEN, "Unable to send registration token. Please try again later!");
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
            throw new AppException(ErrorCode.INVALID_TOKEN, "Feedback không hợp lệ và đã bị xoá!");
        }
        String token = authHeader.substring(7);
        boolean result = authService.logout(token);

        if (result) {
            return APIResponse.<Void>builder()
                            .message("Logout successfully")
                            .result(null)
                            .build();
        } else {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Feedback không hợp lệ và đã bị xoá!");
        }
    }


    /**
     * API xác nhận token từ email và tạo tài khoản (từ link xác nhận trong email).
     */
    @GetMapping("/confirm")
    public ResponseEntity<Object> confirmEmail(@RequestParam("token") String token, HttpServletResponse response) {
        String accessToken = userService.confirmTokenAndCreateUser(token);

        if (accessToken != null) {
            // Tạo cookie chứa token
            Cookie cookie = new Cookie("access_token", accessToken);
            cookie.setHttpOnly(false); // Nếu muốn JS truy cập, set false
            cookie.setSecure(false);   // Set true nếu dùng HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24); // 1 ngày

            response.addCookie(cookie);

            // Redirect về frontend
            String loginUrl = UriComponentsBuilder
                    .fromUriString("http://localhost:8080/tourify")
                    .build().toUriString();

            return ResponseEntity.status(302)
                    .header("Location", loginUrl)
                    .build();
        } else {
            return ResponseEntity.status(302)
                    .header("Location", "http://localhost:8080/tourify/register?error=token_invalid")
                    .build();
        }
    }
    /*
    Sample JSON:
    {
        "token": "<TOKEN>"
    }
    */

    @GetMapping("/me")
    public APIResponse<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Token hết hạn!");
        }

        String token = authHeader.substring(7); // loại bỏ "Bearer "
        UserResponse user = authService.getUserFromToken(token);

        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng");
        }

        return APIResponse.<UserResponse>builder()
                .result(user)
                .message("User information retrieved successfully")
                .build();
    }

    @GetMapping("/oauth2/success")
    public APIResponse<?> loginWithGoogle(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return authService.loginWithGoogle(oAuth2User);
    }

}
