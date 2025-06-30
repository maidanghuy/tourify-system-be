package com.example.tourify_system_be.dto.response;

import com.example.tourify_system_be.entity.User;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TourResponse {
    private String tourId;
    private String tourName;
    private String description;
    private Integer duration;
    private BigDecimal price;
    private Integer minPeople;
    private Integer maxPeople;
    private Integer touristNumberAssigned;
    private String thumbnail;
    private String status;
    private String placeName;
    private String categoryName;

    /**
     * Trung bình đánh giá của tour (tính từ feedbacks).
     * Giá trị mặc định là 5.0 nếu chưa có feedback nào.
     */
    @Builder.Default
    private BigDecimal rating = BigDecimal.valueOf(5.0);

    private String createdByUserName;
    // ✅ Mới thêm: số lượng khách đã đăng ký tour
    private Integer bookedCustomerCount;
    private UserResponse managementBy;
}
