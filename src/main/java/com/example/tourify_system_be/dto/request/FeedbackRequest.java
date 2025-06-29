package com.example.tourify_system_be.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotBlank
    private String tourId;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String content;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private Double rating;
}
