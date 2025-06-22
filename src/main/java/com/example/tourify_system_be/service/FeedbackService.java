package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.response.FeedbackResponse;
import com.example.tourify_system_be.entity.Feedback;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.repository.IFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.tourify_system_be.exception.ErrorCode;



import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FeedbackService {

    private final IFeedbackRepository feedbackRepository;

    public List<FeedbackResponse> getFeedbacksByTour(String tourId) {
        return feedbackRepository.findByTour_TourIdAndStatus(tourId, "approved")
                .stream()
                .map(fb -> FeedbackResponse.builder()
                        .feedbackId(fb.getFeedbackId())
                        .userFullName(fb.getUser().getFullName())
                        .title(fb.getTitle())
                        .content(fb.getContent())
                        .rating(fb.getRating())
                        .createdAt(fb.getCreateAt())
                        .build())
                .toList();
    }
}

