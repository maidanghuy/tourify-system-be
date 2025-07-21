package com.example.tourify_system_be.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UpdatePromotionRequest {
    private Integer quantity;
    private String conditions;
    private Integer discountPercent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Integer minPurchase;
    private String code; // Bắt buộc để kiểm tra khi update
    private List<UUID> tourIds; // Danh sách tour muốn liên kết lại
}
