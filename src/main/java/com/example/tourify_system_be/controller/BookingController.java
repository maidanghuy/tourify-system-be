package com.example.tourify_system_be.controller;
import com.example.tourify_system_be.dto.request.BookingTourRequest;
import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.service.BookingTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingTourService bookingsTourService;

    @PostMapping
    public ResponseEntity<BookingTourResponse> createBooking(@RequestBody BookingTourRequest request) {
        BookingTourResponse createdBooking = bookingsTourService.createBooking(request);
        return ResponseEntity.ok(createdBooking);  // ✅ Trả về DTO, không trả về Entity
    }
}
