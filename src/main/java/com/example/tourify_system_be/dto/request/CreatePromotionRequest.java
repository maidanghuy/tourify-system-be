package com.example.tourify_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePromotionRequest {
    String code;
    Integer quantity;
    String conditions;
    Integer discountPercent;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String description;
    Integer minPurchase;
    List<UUID> tourIds;
}
