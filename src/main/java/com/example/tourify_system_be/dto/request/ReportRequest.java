package com.example.tourify_system_be.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {

    @NotNull(message = "tourId is required")
    private String tourId; // Có thể để kiểu Long nếu khóa là Long, ở đây là String

    // Bỏ @NotNull vì userId sẽ được backend set từ token, không bắt client truyền
    private String userId;

    @NotBlank(message = "reasonCode is required")
    private String reasonCode; // INAPPROPRIATE, FRAUD, OTHER

    private String description; // Optional, mô tả chi tiết
}
