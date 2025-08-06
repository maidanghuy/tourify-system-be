package com.example.tourify_system_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AIInsightsResponse {
    private String trends; // Phân tích xu hướng
    private List<HotTourResponse> hotTours; // Danh sách tour nổi bật
}

