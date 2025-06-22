package com.example.tourify_system_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private String feedbackId;
    private String userFullName;
    private String title;
    private String content;
    private BigDecimal rating;
    private LocalDateTime createdAt;
}
