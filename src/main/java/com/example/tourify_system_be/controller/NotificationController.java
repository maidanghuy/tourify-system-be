package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.NotificationRequest;
import com.example.tourify_system_be.dto.response.NotificationResponse;
import com.example.tourify_system_be.entity.Notification;
import com.example.tourify_system_be.security.JwtUtil;
import com.example.tourify_system_be.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping("")
    public APIResponse<?> getAllNotificationsForUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return APIResponse.builder()
                    .code(401)
                    .message("Missing or invalid Authorization header")
                    .result(null)
                    .build();
        }
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUserId(token);
        return APIResponse.<List<NotificationResponse>>builder()
                .result(notificationService.getAllNotificationsForUser(userId))
                .build();
    }

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastNotification(@RequestBody NotificationRequest request) {
        notificationService.sendToAllUsers(request);
        return ResponseEntity.ok("Notification sent to all users.");
    }
    // SAMPLE JSON
    // {
    // "title": "Thông báo riêng",
    // "message": "Bạn có một khuyến mãi đặc biệt dành riêng cho bạn!",
    // "type": "PROMOTION",
    // "link": "/khuyen-mai/vip",
    // "imageUrl": null,
    // "createdBy": "admin"
    // }

    @PostMapping("/send-to-users")
    public ResponseEntity<?> sendToUsers(@RequestBody NotificationRequest request) {
        notificationService.sendToUsers(request);
        return ResponseEntity.ok("Notification sent to selected users.");
    }

    @PostMapping("/read")
    public APIResponse<?> markNotificationsAsRead(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<String> notificationIds) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return APIResponse.builder()
                    .code(401)
                    .message("Missing or invalid Authorization header")
                    .result(null)
                    .build();
        }
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUserId(token);
        notificationService.markAsRead(notificationIds, userId);
        return APIResponse.builder()
                .message("Marked as read")
                .result(null)
                .build();
    }

}
