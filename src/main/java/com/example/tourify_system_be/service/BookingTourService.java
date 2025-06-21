package com.example.tourify_system_be.service;
import com.example.tourify_system_be.dto.request.BookingTourRequest;
import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.entity.BookingsTour;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.repository.BookingTourRepository;
import com.example.tourify_system_be.repository.TourRepository;
import com.example.tourify_system_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingTourService {

    private final BookingTourRepository bookingTourRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final EmailService emailService; // ✅ THÊM DÒNG NÀY

    public BookingTourResponse createBooking(BookingTourRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));

        // ✅ Validate ngày
        if (request.getDayEnd().isBefore(request.getDayStart())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }

        // ✅ Validate số người
        int totalPeople = request.getAdultNumber() + request.getChildNumber();
        if (totalPeople > tour.getMaxPeople()) {
            throw new IllegalArgumentException("Số người vượt quá giới hạn của tour này.");
        }

        // ✅ Tính giá
        BigDecimal totalPrice = tour.getPrice().multiply(BigDecimal.valueOf(totalPeople));

        // ✅ Tạo booking
        BookingsTour booking = BookingsTour.builder()
                .user(user)
                .tour(tour)
                .adultNumber(request.getAdultNumber())
                .childNumber(request.getChildNumber())
                .dayStart(request.getDayStart())
                .dayEnd(request.getDayEnd())
                .totalPrice(totalPrice.intValue())
                .status("booked")
                .createdAt(LocalDateTime.now())
                .build();

        bookingTourRepository.save(booking);

        emailService.sendBookingConfirmationEmail(
                user.getEmail(),
                user.getFullName(),
                tour.getTourName(),
                totalPrice.intValue(),
                request.getDayStart().toLocalDate().toString(),
                request.getDayEnd().toLocalDate().toString()
        );

        // ✅ Gửi email (giả lập)
        log.info("Gửi email đến {}: Xác nhận đặt tour {} cho {} người. Tổng giá: {} VND.",
                user.getEmail(), tour.getTourName(), totalPeople, totalPrice);
        return BookingTourResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(user.getUserId())
                .userName(user.getFullName())
                .tourId(tour.getTourId())
                .tourName(tour.getTourName())
                .adultNumber(booking.getAdultNumber())
                .childNumber(booking.getChildNumber())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .dayStart(booking.getDayStart())
                .dayEnd(booking.getDayEnd())
                .build();
    }
}
