package com.example.tourify_system_be.dto.request;

import com.example.tourify_system_be.entity.Category;
import com.example.tourify_system_be.entity.Place;
import com.example.tourify_system_be.entity.User;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TourCreateRequest {

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

    @Min(0)
    private int touristNumberAssigned;


    // Không nên truyền từ client, sẽ được set từ backend khi lưu vào DB
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Pattern(regexp = "^(ACTIVE|INACTIVE|DRAFT)$", message = "Status must be ACTIVE, INACTIVE or DRAFT")
    private String status;

    @NotBlank
    private String place;

    @NotBlank
    private String category;


    private String thumbnail;
}
