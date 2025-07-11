package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.FeedbackRequest;
import com.example.tourify_system_be.dto.response.FeedbackResponse;
import com.example.tourify_system_be.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * Lấy danh sách feedback cho 1 tour.
     * User chỉ xem feedback đã duyệt, sub-company & admin xem tất cả.
     * Header Authorization không bắt buộc để khách cũng xem được feedback hợp lệ.
     */
    @GetMapping("/{tourId}")
    public ResponseEntity<List<FeedbackResponse>> getByTour(
            @PathVariable String tourId,
            @RequestParam(required = false) String userId
    ) {
        List<FeedbackResponse> responses = feedbackService.getFeedbacksByTourWithUserId(tourId, userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Khách hàng gửi feedback (tự động duyệt nếu hợp lệ, reject và xóa nếu không hợp lệ).
     */
    @PostMapping
    public ResponseEntity<FeedbackResponse> add(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody @Validated FeedbackRequest request
    ) {
        FeedbackResponse resp = feedbackService.addFeedback(authorization, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{tourId}/latest")
    public ResponseEntity<FeedbackResponse> getLatestApproved(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable String tourId
    ) {
        FeedbackResponse latest = feedbackService.getLatestApprovedFeedback(authorization, tourId);
        if (latest == null) {
            // Có thể trả 204 No Content hoặc 404 tùy ý
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(latest);
    }
}
