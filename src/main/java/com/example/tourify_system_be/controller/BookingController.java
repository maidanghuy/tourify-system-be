package com.example.tourify_system_be.controller;
import com.example.tourify_system_be.dto.request.BookingTourRequest;
import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.service.BookingTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.tourify_system_be.security.CustomUserDetails;
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingTourService bookingsTourService;

    @PostMapping
    public ResponseEntity<BookingTourResponse> createBooking(
            @RequestBody BookingTourRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) { // ✅ LẤY USER TỪ TOKEN

        BookingTourResponse createdBooking = bookingsTourService.createBooking(request, currentUser.getId());
        return ResponseEntity.ok(createdBooking);
    }
}
