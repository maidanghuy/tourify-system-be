package com.example.tourify_system_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TourBookingResponse {
    private String bookingId;

    // Thông tin tour
    private String tourId;
    private String tourName;
    private String tourLocation;
    private String tourImageUrl;

    // Số lượng người
    private Integer adultNumber;
    private Integer childNumber;

    // Ngày tour
    private LocalDateTime dayStart;
    private LocalDateTime dayEnd;

    // Trạng thái đặt
    private String status;

    // Tổng giá tiền
    private Integer totalPrice;

    // Thời gian tạo booking
    private LocalDateTime createdAt;
}
