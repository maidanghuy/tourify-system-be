package com.example.tourify_system_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private String feedbackId;
    private String userFullName;
    private String avatar;     // đường dẫn ảnh user
    private String role;       // đúng tên trường trong User entity
    private String title;
    private String content;
    private Double rating;
    private LocalDateTime createdAt;
    private String status;
}
