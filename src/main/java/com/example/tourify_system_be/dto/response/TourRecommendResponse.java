package com.example.tourify_system_be.dto.response;

import com.example.tourify_system_be.entity.Tour;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TourRecommendResponse {
    private String tourId;
    private String tourName;
    private String imageUrl;
    private String placeName;
    private String categoryName;
    private BigDecimal price;

    public static TourRecommendResponse fromTour(Tour t) {
        return TourRecommendResponse.builder()
                .tourId(t.getTourId())
                .tourName(t.getTourName())
                .imageUrl(t.getThumbnail())
                .placeName(t.getPlace() != null ? t.getPlace().getPlaceName() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getCategoryName() : null)
                .price(t.getPrice())
                .build();
    }
}

