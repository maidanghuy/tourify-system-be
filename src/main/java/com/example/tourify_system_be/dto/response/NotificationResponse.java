package com.example.tourify_system_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String title;
    private String message;
    private String type;
    private String link;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String createdBy;
    private Boolean isRead;
}