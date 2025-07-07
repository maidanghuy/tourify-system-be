package com.example.tourify_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionResponse {
    String promotionId;
    String code;
    Integer quantity;
    String conditions;
    Integer discountPercent;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String status;
    String description;
    Integer minPurchase;
    String createdBy; // Tên hoặc ID người tạo (tùy theo bạn muốn show gì)
}
