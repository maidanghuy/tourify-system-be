package com.example.tourify_system_be.service;
import com.example.tourify_system_be.dto.request.BookingCancelRequest;
import com.example.tourify_system_be.dto.request.BookingTourRequest;
import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.entity.BookingTour;
import com.example.tourify_system_be.entity.TokenAuthentication;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.repository.BookingTourRepository;
import com.example.tourify_system_be.repository.TokenAuthenticationRepository;
import com.example.tourify_system_be.repository.TourRepository;
import com.example.tourify_system_be.repository.UserRepository;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.security.JwtUtil;
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
    private final EmailService emailService;
    private final TokenAuthenticationRepository tokenAuthenticationRepository;
    private final JwtUtil jwtUtil;

    public BookingTourResponse createBooking(BookingTourRequest request, String token) {
        // Tách token khỏi chuỗi "Bearer ..."
        String tokenValue = token.startsWith("Bearer ") ? token.substring(7) : token;

        // Tìm token trong DB
        TokenAuthentication tokenAuth = tokenAuthenticationRepository.findByTokenValue(tokenValue)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        // Kiểm tra nếu token đã hết hạn hoặc bị vô hiệu
        TokenAuthentication session = tokenAuthenticationRepository.findByTokenValue(tokenValue)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_EXPIRED));
        if (!session.getIsUsed()) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        // Trích xuất thông tin user
        User user = tokenAuth.getUser();

        // Kiểm tra trạng thái tài khoản
        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_DISABLED);
        }

        // Chỉ user mới được book tour
        if (!"user".equalsIgnoreCase(user.getRole())) {
            throw new AppException(ErrorCode.BOOKING_FORBIDDEN_ROLE);
        }

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        // Trạng thái tour phải là "active" thì mới đặt tour được
        if (!"active".equalsIgnoreCase(tour.getStatus())) {
            throw new AppException(ErrorCode.TOUR_NOT_ACTIVE);
        }

        // Ngày kết thúc = ngày bắt đầu + duration
        LocalDateTime dayEnd = request.getDayStart().plusDays(tour.getDuration());
        // Validate ngày, phải đặt tour sau 4 ngày so với hiện tại thì mới đặt được
        LocalDateTime now = LocalDateTime.now();
        if (request.getDayStart().isBefore(now.plusDays(4))) {
            throw new AppException(ErrorCode.INVALID_BOOKING_DATE);
        }


        // Validate số người
        if (request.getAdultNumber() == null || request.getChildNumber() == null
                || request.getAdultNumber() < 0 || request.getChildNumber() < 0) {
            throw new AppException(ErrorCode.INVALID_PEOPLE_COUNT);
        }
        int totalPeople = request.getAdultNumber() + request.getChildNumber();
        if (totalPeople > tour.getMaxPeople()) {
            throw new AppException(ErrorCode.EXCEED_MAX_PEOPLE);
        }
        if (totalPeople < tour.getMinPeople()) {
            throw new AppException(ErrorCode.BELOW_MIN_PEOPLE);
        }

        // ✅ Cập nhật số người đã được đặt cho tour
        tour.setTouristNumberAssigned(
                tour.getTouristNumberAssigned() == null ? totalPeople : tour.getTouristNumberAssigned() + totalPeople
        );
        tourRepository.save(tour);

        // ✅ Tính giá
        BigDecimal totalPrice = tour.getPrice().multiply(BigDecimal.valueOf(totalPeople));

        // ✅ Tạo booking
        BookingTour booking = BookingTour.builder()
                .user(user)
                .tour(tour)
                .adultNumber(request.getAdultNumber())
                .childNumber(request.getChildNumber())
                .dayStart(request.getDayStart())
                .dayEnd(dayEnd)
                .totalPrice(totalPrice.intValue())
                .status("Pending")
                .createdAt(LocalDateTime.now())
                .build();

        bookingTourRepository.save(booking);

        emailService.sendBookingConfirmationEmail(
                user.getEmail(),
                user.getFullName(),
                tour.getTourName(),
                totalPrice.intValue(),
                request.getDayStart().toLocalDate().toString(),
                dayEnd.toLocalDate().toString()
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
                .thumbnail(tour.getThumbnail())
                .build();
    }

    public BookingTourResponse cancelBooking(BookingCancelRequest request, String token) {
        // 1. Kiểm tra JWT token hợp lệ và lấy userId
        String jwt = token.replace("Bearer ", "");
        if (!jwtUtil.validateToken(jwt)) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        String userId = jwtUtil.extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra phiên đăng nhập có còn hợp lệ không
        TokenAuthentication session = tokenAuthenticationRepository.findByTokenValue(jwt)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_EXPIRED));
        if (!session.getIsUsed()) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }


        // 3. Tìm booking cần huỷ
        BookingTour booking = bookingTourRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 4. Chỉ huỷ nếu trạng thái là "pending" hoặc "confirmed"
        if (!booking.getStatus().equalsIgnoreCase("pending") &&
                !booking.getStatus().equalsIgnoreCase("confirmed")) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }

        // 5. Cập nhật trạng thái
        booking.setStatus("cancelled");
        bookingTourRepository.save(booking);
//        booking.setCancelReason(request.getReason());
        return BookingTourResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(user.getUserId())
                .userName(user.getFullName())
                .tourId(booking.getTour().getTourId())
                .tourName(booking.getTour().getTourName())
                .adultNumber(booking.getAdultNumber())
                .childNumber(booking.getChildNumber())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .dayStart(booking.getDayStart())
                .dayEnd(booking.getDayEnd())
                .thumbnail(booking.getTour().getThumbnail())
                .build();
    }

    public void markAsPaid(long orderCode) {
        String bookingId = String.valueOf(orderCode);
        BookingTour booking = bookingTourRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        booking.setStatus("PAID");
        bookingTourRepository.save(booking);
    }
}
