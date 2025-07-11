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
import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FeedbackService {

    private final IFeedbackRepository feedbackRepository;
    private final JwtUtil jwtUtil;
    private final ITourRepository tourRepo;
    private final IUserRepository userRepo;

    private static final List<String> BAD_WORDS = List.of(
            // ... danh sách từ cấm ...
            "fuck", "shit", "bitch", "asshole", "bastard", "dick", "cunt", "fag", "slut", "whore", "motherfucker",
            "son of a bitch", "cock", "pussy", "douche", "douchebag", "bollocks", "bugger", "bloody", "damn", "crap",
            "wanker", "twat", "prick", "nigger", "nigga", "retard", "moron", "idiot", "stupid", "jackass",
            "fucking", "fucked", "fucks", "shitty", "shithead", "bullshit", "dickhead", "arsehole",
            "xxx", "badword",
            // Tiếng Việt
            "địt", "cặc", "lồn", "đụ", "vcl", "clm", "vãi", "bố mày", "con mẹ mày", "đéo", "cmm", "má mày",
            "thằng chó", "ngu", "óc chó", "đần", "con chó", "chó má", "con đĩ", "con cặc"
    );

    private boolean isFeedbackValid(FeedbackRequest req) {
        if (req.getTitle() == null || req.getTitle().trim().length() < 5) return false;
        if (req.getContent() == null || req.getContent().trim().length() < 10) return false;
        if (req.getRating() == null || req.getRating() < 1.0 || req.getRating() > 5.0) return false;

        String all = (req.getTitle() + " " + req.getContent()).toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\sàáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđ]", " ");
        String[] words = all.split("\\s+");

        for (String bad : BAD_WORDS) {
            for (String word : words) {
                if (word.equals(bad) || word.startsWith(bad)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByTour(String bearerToken, String tourId) {
        if (!tourRepo.existsById(tourId)) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND, "Không tìm thấy tour.");
        }

        boolean isAdminOrSubCompany = false;

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            try {
                String jwt = bearerToken.substring(7);
                jwtUtil.validateToken(jwt); // có thể throw exception nếu token không hợp lệ
                CustomUserDetails user = jwtUtil.getUserDetailsFromToken(jwt);
                String role = Optional.ofNullable(user.getRole())
                        .map(String::trim)
                        .orElse("")
                        .toUpperCase(Locale.ROOT);
                isAdminOrSubCompany = "ADMIN".equals(role) || "SUB_COMPANY".equals(role);
            } catch (Exception e) {
                // Token không hợp lệ, coi như là khách (không phải admin/sub-company)
                isAdminOrSubCompany = false;
            }
        }

        List<Feedback> feedbacks;
        if (isAdminOrSubCompany) {
            // Admin hoặc Sub-company được xem tất cả feedback (không lọc status)
            feedbacks = feedbackRepository.findByTour_TourId(tourId);
        } else {
            // Khách và các vai trò khác chỉ xem feedback hợp lệ (đã duyệt)
            feedbacks = feedbackRepository.findByTour_TourIdAndStatus(tourId, "APPROVED");
        }

        return feedbacks.stream()
                .map(this::mapFeedbackToResponse)
                .toList();
    }

    @Transactional
    public FeedbackResponse addFeedback(String bearerToken, FeedbackRequest req) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Bạn chưa đăng nhập hoặc phiên đăng nhập hết hạn.");
        }
        String jwt = bearerToken.substring(7);
        jwtUtil.validateToken(jwt);
        CustomUserDetails userDetails = jwtUtil.getUserDetailsFromToken(jwt);

        String role = Optional.ofNullable(userDetails.getRole()).orElse("");
        if (!"USER".equalsIgnoreCase(role)) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED, "Chỉ người dùng mới có thể gửi feedback.");
        }

        User user = userRepo.findById(userDetails.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng."));
        Tour tour = tourRepo.findById(req.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND, "Không tìm thấy tour."));

        boolean valid = isFeedbackValid(req);
        String status = valid ? "APPROVED" : "REJECTED";

        Feedback fb = Feedback.builder()
                .user(user)
                .tour(tour)
                .title(req.getTitle())
                .content(req.getContent())
                .rating(BigDecimal.valueOf(req.getRating()))
                .status(status)
                .createAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        feedbackRepository.save(fb);

        if (!valid) {
            feedbackRepository.delete(fb);
            throw new AppException(ErrorCode.INVALID_FEEDBACK, "Feedback không hợp lệ (quá ngắn, không đúng nội dung hoặc chứa từ cấm) và đã bị xoá!");
        }

        return mapFeedbackToResponse(fb);
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getLatestApprovedFeedback(String bearerToken, String tourId) {
        if (!tourRepo.existsById(tourId)) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND, "Không tìm thấy tour.");
        }

        boolean isAdminOrSubCompany = false;

        // Kiểm tra token và role nếu có, xử lý ngoại lệ khi validate thất bại
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            try {
                String jwt = bearerToken.substring(7);
                jwtUtil.validateToken(jwt);
                CustomUserDetails user = jwtUtil.getUserDetailsFromToken(jwt);
                String role = Optional.ofNullable(user.getRole()).map(String::trim).orElse("").toUpperCase(Locale.ROOT);
                isAdminOrSubCompany = "ADMIN".equals(role) || "SUB_COMPANY".equals(role);
            } catch (Exception e) {
                // Token không hợp lệ, coi như không phải admin/sub-company
                isAdminOrSubCompany = false;
            }
        }

        Optional<Feedback> fbOptional;

        if (isAdminOrSubCompany) {
            // Trả về feedback mới nhất bất kể trạng thái
            fbOptional = feedbackRepository.findTopByTour_TourIdOrderByCreateAtDesc(tourId);
        } else {
            // Chỉ trả về feedback mới nhất có trạng thái APPROVED
            fbOptional = feedbackRepository.findTopByTour_TourIdAndStatusOrderByCreateAtDesc(tourId, "APPROVED");
        }

        if (fbOptional.isEmpty()) {
            return null;
        }

        return mapFeedbackToResponse(fbOptional.get());
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByTourWithUserId(String tourId, String userId) {
        if (!tourRepo.existsById(tourId)) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND, "Không tìm thấy tour.");
        }
        boolean isPrivileged = false;
        if (userId != null) {
            // Tra role từ DB
            Optional<User> userOpt = userRepo.findById(userId);
            if (userOpt.isPresent()) {
                String role = userOpt.get().getRole();
                isPrivileged = "SUB_COMPANY".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
            }
        }
        List<Feedback> feedbacks;
        if (isPrivileged) {
            feedbacks = feedbackRepository.findByTour_TourId(tourId);
        } else {
            feedbacks = feedbackRepository.findByTour_TourIdAndStatus(tourId, "APPROVED");
        }
        return feedbacks.stream()
                .map(this::mapFeedbackToResponse)
                .toList();
    }

    // Helper: mapping entity Feedback -> FeedbackResponse
    private FeedbackResponse mapFeedbackToResponse(Feedback fb) {
        User user = fb.getUser();
        return FeedbackResponse.builder()
                .feedbackId(fb.getFeedbackId())
                .userFullName(user.getFullName())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .title(fb.getTitle())
                .content(fb.getContent())
                .rating(fb.getRating().doubleValue())
                .createdAt(fb.getCreateAt())
                .status(fb.getStatus())
                .build();
    }
}
