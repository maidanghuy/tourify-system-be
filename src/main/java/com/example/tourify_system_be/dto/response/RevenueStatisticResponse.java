package com.example.tourify_system_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueStatisticResponse {
    private String time;        // ngày/tháng/năm dạng text
    private Long totalRevenue;  // tổng doanh thu
}
