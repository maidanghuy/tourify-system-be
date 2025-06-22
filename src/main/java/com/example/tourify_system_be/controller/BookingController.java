package com.example.tourify_system_be.controller;
import com.example.tourify_system_be.dto.request.BookingTourRequest;
import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.service.BookingTourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingTourService bookingsTourService;

    @PostMapping
    public ResponseEntity<BookingTourResponse> createBooking(
            @Valid
            @RequestBody BookingTourRequest request,
            @RequestHeader("Authorization") String token) {

        BookingTourResponse createdBooking = bookingsTourService.createBooking(request, token);
        return ResponseEntity.ok(createdBooking);
    }
}
