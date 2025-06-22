package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.response.FeedbackResponse;
import com.example.tourify_system_be.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByTour(@PathVariable String tourId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByTour(tourId));
    }
}
