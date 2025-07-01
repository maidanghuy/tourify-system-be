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

    @GetMapping("/{tourId}")
    public List<FeedbackResponse> getByTour(
            @RequestHeader("Authorization") String token,
            @PathVariable String tourId) {
        return feedbackService.getFeedbacksByTour(token, tourId);
    }


    /**
     * Khách hàng gửi feedback (status sẽ là pending, chờ admin duyệt)
     */
    @PostMapping
    public ResponseEntity<FeedbackResponse> add(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody @Validated FeedbackRequest request) {

        var resp = feedbackService.addFeedback(authorization, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * Lấy feedback mới nhất đã được duyệt (APPROVED) cho một tour
     */
    @GetMapping("/{tourId}/latest-approved")
    public ResponseEntity<FeedbackResponse> getLatestApproved(
            @PathVariable String tourId) {

        FeedbackResponse latest = feedbackService.getLatestApprovedFeedback(tourId);
        return ResponseEntity.ok(latest);
    }
}

