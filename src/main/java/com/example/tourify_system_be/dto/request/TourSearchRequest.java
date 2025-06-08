package com.example.tourify_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourSearchRequest {
    String placeName;               // Tên địa điểm
    Integer duration;               // Thời gian tour
    String categoryName;            // Loại tour (theo tên)
    Integer touristNumberAssigned;  // Số lượng khách đã đặt
}
