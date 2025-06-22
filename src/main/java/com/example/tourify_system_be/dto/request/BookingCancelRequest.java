package com.example.tourify_system_be.dto.request;

import lombok.Data;

@Data
public class BookingCancelRequest {
    private String bookingId;
    private String reason; // optional
}