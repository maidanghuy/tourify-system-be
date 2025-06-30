package com.example.tourify_system_be.dto.request;
import lombok.*;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTourRequest {
    private String tourId;
    private Integer adultNumber;
    private Integer childNumber;
    private LocalDateTime dayStart;
}
