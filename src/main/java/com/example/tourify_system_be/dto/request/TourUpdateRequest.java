package com.example.tourify_system_be.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TourUpdateRequest {
    @NotBlank
    private String tourName;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @Min(1)
    private int duration;

    @Min(1)
    private int minPeople;

    @Min(1)
    private int maxPeople;

    @Pattern(regexp = "^(ACTIVE|INACTIVE|DRAFT)$", message = "Status must be ACTIVE, INACTIVE or DRAFT")
    private String status;

    private String place;

    @NotBlank
    private String category;

    private String thumbnail;
    private List<String> activityIds;
    private List<String> serviceIds;

    // Có thể cho phép update startDate nếu business cho phép
    private String startDate;
}

