package com.example.tourify_system_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HotTourResponse {
    private String tourId;
    private String tourName;
    private int bookingCount;
    private String reason; // Lý do được gợi ý (tăng trưởng, rating cao,...)
}
