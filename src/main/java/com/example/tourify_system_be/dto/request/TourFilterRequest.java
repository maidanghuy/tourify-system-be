package com.example.tourify_system_be.dto.request;

import com.example.tourify_system_be.dto.response.TourResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourFilterRequest {
    List<TourResponse> baseTours; // Danh sách tour đã search ra

    BigDecimal minPrice;
    BigDecimal maxPrice;

    BigDecimal minRating;
    BigDecimal maxRating;

    String createdByUserName;
}
