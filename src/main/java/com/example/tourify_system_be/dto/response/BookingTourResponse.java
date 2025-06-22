package com.example.tourify_system_be.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTourResponse {
    private String bookingId;
    private String userId;
    private String userName;
    private String tourId;
    private String tourName;
    private Integer adultNumber;
    private Integer childNumber;
    private LocalDateTime dayStart;
    private LocalDateTime dayEnd;
    private Integer totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private String thumbnail;
}
