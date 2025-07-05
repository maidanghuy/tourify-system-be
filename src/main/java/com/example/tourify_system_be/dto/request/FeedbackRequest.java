package com.example.tourify_system_be.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotBlank(message = "Tour ID must not be blank")
    private String tourId;

    @NotBlank(message = "Title must not be blank")
    @Size(min = 5, max = 100, message = "Title must be from 5 to 100 characters")
    private String title;

    @NotBlank(message = "Content must not be blank")
    @Size(min = 10, max = 1000, message = "Content must be from 10 to 1000 characters")
    private String content;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    private Double rating;
}
