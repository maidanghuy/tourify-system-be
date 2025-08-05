package com.example.tourify_system_be.controller;
import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.BookingCancelRequest;
import com.example.tourify_system_be.dto.request.BookingTourRequest;
import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.entity.BookingTour;
import com.example.tourify_system_be.service.BookingTourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingTourService bookingsTourService;

    @PostMapping
    public APIResponse<?> createBooking(
            @Valid
            @RequestBody BookingTourRequest request,
            @RequestHeader("Authorization") String token) {

        BookingTourResponse createdBooking = bookingsTourService.createBooking(request, token);
        return APIResponse.<BookingTourResponse>builder()
                .message("Booking successfully")
                .result(createdBooking)
                .build();
    }

    @PutMapping("/cancel")
    public APIResponse<?> cancelBooking(
            @RequestBody BookingCancelRequest request,
            @RequestHeader("Authorization") String token
    ) {
        BookingTourResponse cancelBooking = bookingsTourService.cancelBooking(request, token);
        return APIResponse.<BookingTourResponse>builder()
                .message("Cancel tour successfully")
                .result(cancelBooking)
                .build();
    }

    @GetMapping("/status/{bookingId}")
    public ResponseEntity<?> getBookingStatus(@PathVariable String bookingId) {
        BookingTour booking = bookingsTourService.findByBookingId(bookingId);
        return ResponseEntity.ok(Map.of("status", booking.getStatus()));
    }


}


