package com.example.tourify_system_be.dto.response;

import com.example.tourify_system_be.entity.*;
import lombok.Builder;
import lombok.Data;
import com.example.tourify_system_be.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourResponse {
    String tourId;
    String tourName;
    String description;
    Integer duration;
    BigDecimal price;
    Integer minPeople;
    Integer maxPeople;
    Integer touristNumberAssigned;
    String thumbnail;
    String status;
    String placeName;
    String categoryName;
    LocalDateTime createdAt;
    LocalDateTime startDate;
    private List<ActivityDTO> activities;   // Sửa lại kiểu này
    private List<ServiceDTO> services;      // Sửa lại kiểu này

    // Inner DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDTO {
        private String activityId;
        private String name;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceDTO {
        private String serviceId;
        private String name;
    }
    /**
     * Trung bình đánh giá của tour (tính từ feedbacks).
     * Giá trị mặc định là 5.0 nếu chưa có feedback nào.
     */
    @Builder.Default
    BigDecimal rating = BigDecimal.valueOf(5.0);

    String createdByUserName;
    // ✅ Mới thêm: số lượng khách đã đăng ký tour
    Integer bookedCustomerCount;
    UserResponse managementBy;
    @Builder.Default
    boolean myTour = false;
}
