package com.example.tourify_system_be.dto.response;

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
    private BigDecimal rating;
    private String createdByUserName;
}
