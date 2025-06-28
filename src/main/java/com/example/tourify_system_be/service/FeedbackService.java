package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.FeedbackRequest;
import com.example.tourify_system_be.dto.response.FeedbackResponse;
import com.example.tourify_system_be.entity.Feedback;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.repository.IFeedbackRepository;
import com.example.tourify_system_be.repository.ITourRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import com.example.tourify_system_be.security.CustomUserDetails;
import com.example.tourify_system_be.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FeedbackService {

    private final IFeedbackRepository feedbackRepository;
    private final JwtUtil jwtUtil;
    private final ITourRepository tourRepo;
    private final IUserRepository userRepo;

    /**
     * Lấy feedbacks cho tour, chỉ ADMIN hoặc SUB_COMPANY mới được phép.
     * @param bearerToken header "Bearer xxx"
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByTour(String bearerToken, String tourId) {
        // 1) Tách token, parse userDetails
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }
        String jwt = bearerToken.substring(7);
        jwtUtil.validateToken(jwt);
        CustomUserDetails user = jwtUtil.getUserDetailsFromToken(jwt);
        String role = user.getRole();

        // 2) Chỉ cho ADMIN hoặc SUB_COMPANY
        if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("SUB_COMPANY")) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // 3) Lấy feedback
        List<Feedback> list = feedbackRepository.findByTour_TourIdAndStatus(tourId, "approved");
        if (list.isEmpty()) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        // 4) Map về DTO
        return list.stream()
                .map(fb -> FeedbackResponse.builder()
                        .feedbackId(fb.getFeedbackId())
                        .userFullName(fb.getUser().getFullName())
                        .title(fb.getTitle())
                        .content(fb.getContent())
                        .rating(fb.getRating().doubleValue())  // FIX: chuyển BigDecimal → double
                        .createdAt(fb.getCreateAt())
                        .status(fb.getStatus())
                        .build())
                .toList();
    }


    @Transactional
    public FeedbackResponse addFeedback(String bearerToken, FeedbackRequest req) {
        // 1) xác thực token
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }
        String jwt = bearerToken.substring(7);
        jwtUtil.validateToken(jwt);
        CustomUserDetails userDetails = jwtUtil.getUserDetailsFromToken(jwt);

        // 2) chỉ cho role = USER
        String role = userDetails.getRole();
        if (!"USER".equalsIgnoreCase(role) && !role.equals("ADMIN")) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // 3) load user & tour
        User user = userRepo.findById(userDetails.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Tour tour = tourRepo.findById(req.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // 4) xây feedback
        Feedback fb = Feedback.builder()
                .user(user)
                .tour(tour)
                .title(req.getTitle())
                .content(req.getContent())
                .rating(BigDecimal.valueOf(req.getRating()))
                .status("pending")
                .createAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        feedbackRepository.save(fb);

        // 5) trả về DTO
        return FeedbackResponse.builder()
                .feedbackId(fb.getFeedbackId())
                .userFullName(user.getFullName())
                .title(fb.getTitle())
                .content(fb.getContent())
                .rating(fb.getRating().doubleValue())
                .createdAt(fb.getCreateAt())
                .status(fb.getStatus())
                .build();
    }
}
