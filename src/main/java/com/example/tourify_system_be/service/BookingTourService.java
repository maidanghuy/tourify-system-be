package com.example.tourify_system_be.service;
import com.example.tourify_system_be.dto.request.BookingCancelRequest;
import com.example.tourify_system_be.dto.request.BookingTourRequest;
import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.entity.BookingTour;
import com.example.tourify_system_be.entity.TokenAuthentication;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.repository.*;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class BookingTourService {

    private final IBookingTourRepository bookingTourRepository;
    private final IUserRepository iUserRepository;
    private final ITourRepository iTourRepository;
    private final EmailService emailService;
    private final ITokenAuthenticationRepository iTokenAuthenticationRepository;
    private final JwtUtil jwtUtil;

    public BookingTourResponse createBooking(BookingTourRequest request, String token) {
        // Tách token khỏi chuỗi "Bearer ..."
        String tokenValue = token.startsWith("Bearer ") ? token.substring(7) : token;

        // Tìm token trong DB
        TokenAuthentication tokenAuth = iTokenAuthenticationRepository.findByTokenValue(tokenValue);
        if (tokenAuth == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Không tim thấy token trong DB!");
        }

        // Kiểm tra nếu token đã hết hạn hoặc bị vô hiệu
        TokenAuthentication session = iTokenAuthenticationRepository.findByTokenValue(tokenValue);
        if (session == null || !session.getIsUsed()) {
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Token này không tồn tại trong hệ thống!");
        }
        if (!session.getIsUsed()) {
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Hết phiên đăng nhập!");
        }

        // Trích xuất thông tin user
        User user = tokenAuth.getUser();

        // Kiểm tra trạng thái tài khoản
        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_DISABLED, "Tài khoản đã bị khóa!");
        }

        // Chỉ user và admin mới được book tour
        if (!List.of("user", "admin").contains(user.getRole().toLowerCase())) {
            throw new AppException(ErrorCode.BOOKING_FORBIDDEN_ROLE, "Bạn không có quyền đặt tour!");
        }

        Tour tour = iTourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND, "Không thể tìm thấy tour!"));
        // Trạng thái tour phải là "active" thì mới đặt tour được
        if (!"active".equalsIgnoreCase(tour.getStatus())) {
            throw new AppException(ErrorCode.TOUR_NOT_ACTIVE, "Tour đang bị khóa!");
        }

        // Ngày kết thúc = ngày bắt đầu + duration
        LocalDateTime dayEnd = request.getDayStart().plusDays(tour.getDuration());
        // Validate ngày, phải đặt tour sau 4 ngày so với hiện tại thì mới đặt được
        LocalDateTime now = LocalDateTime.now();
        if (request.getDayStart().isBefore(now.plusDays(4))) {
            throw new AppException(ErrorCode.INVALID_BOOKING_DATE, "Phải sau 4 ngày so với hiện tại thì mới được đặt tour");
        }


        // Validate số người
        if (request.getAdultNumber() == null || request.getChildNumber() == null
                || request.getAdultNumber() < 0 || request.getChildNumber() < 0) {
            throw new AppException(ErrorCode.INVALID_PEOPLE_COUNT, "Số người lớn khng được bé hơn 1!");
        }
        int totalPeople = request.getAdultNumber() + request.getChildNumber();
        if (totalPeople > tour.getMaxPeople()) {
            throw new AppException(ErrorCode.EXCEED_MAX_PEOPLE, "Đã vượt quá số người cho phép!");
        }
        if (totalPeople < tour.getMinPeople()) {
            throw new AppException(ErrorCode.BELOW_MIN_PEOPLE, "Không được ít hơn số người tối thiểu!");
        }


        // ✅ Cập nhật số người đã được đặt cho tour
        tour.setTouristNumberAssigned(
                tour.getTouristNumberAssigned() == null
                        ? totalPeople
                        : tour.getTouristNumberAssigned() + totalPeople
        );
        iTourRepository.save(tour);

// ✅ Tính giá mới
        BigDecimal basePrice = tour.getPrice();
        BigDecimal adultRate = new BigDecimal("0.2");
        BigDecimal childRate = new BigDecimal("0.15");

        BigDecimal adultSurcharge = basePrice
                .multiply(BigDecimal.valueOf(request.getAdultNumber()))
                .multiply(adultRate);
        BigDecimal childSurcharge = basePrice
                .multiply(BigDecimal.valueOf(request.getChildNumber()))
                .multiply(childRate);

        BigDecimal totalPrice = basePrice
                .add(adultSurcharge)
                .add(childSurcharge);

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

//         ✅ Gửi email (giả lập)
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
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Feedback không hợp lệ và đã bị xoá!");
        }

        String userId = jwtUtil.extractUserId(jwt);
        User user = iUserRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));

        // 2. Kiểm tra phiên đăng nhập có còn hợp lệ không
        TokenAuthentication session = iTokenAuthenticationRepository.findByTokenValue(jwt);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Feedback không hợp lệ và đã bị xoá!");
        }
        if (!session.getIsUsed()) {
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Feedback không hợp lệ và đã bị xoá!");
        }


        // 3. Tìm booking cần huỷ
        BookingTour booking = bookingTourRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));

        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Feedback không hợp lệ và đã bị xoá!");
        }

        // 4. Chỉ huỷ nếu trạng thái là "pending" hoặc "confirmed"
        if (!booking.getStatus().equalsIgnoreCase("pending") &&
                !booking.getStatus().equalsIgnoreCase("confirmed")) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS, "Feedback không hợp lệ và đã bị xoá!");
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

    public BookingTour findByBookingId(String bookingId) {
        return bookingTourRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }
}
