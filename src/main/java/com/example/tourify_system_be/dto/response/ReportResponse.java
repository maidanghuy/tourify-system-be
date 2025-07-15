package com.example.tourify_system_be.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private Long reportId;

    private String tourId;
    private String tourName;     // Gợi ý: trả kèm tên tour để client dễ hiển thị

    private String userId;
    private String userName;     // Gợi ý: trả kèm tên user (ẩn email, số điện thoại)

    private String reasonCode;   // INAPPROPRIATE, FRAUD, OTHER
    private String description;

    private String status;       // PENDING, RESOLVED, REJECTED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Có thể bổ sung các trường mở rộng như adminComment nếu DB có
    // private String adminComment;
}
