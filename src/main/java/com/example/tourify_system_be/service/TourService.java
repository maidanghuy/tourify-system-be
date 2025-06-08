package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.TourFilterRequest;
import com.example.tourify_system_be.dto.request.TourSearchRequest;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.mapper.TourMapper;
import com.example.tourify_system_be.repository.FeedbackRepository;
import com.example.tourify_system_be.repository.ITourRepository;
import com.example.tourify_system_be.specification.TourSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourService {

    private final ITourRepository itourRepository;
    private final FeedbackRepository feedbackRepository;
    private final TourMapper tourMapper;

    public List<TourResponse> searchTours(TourSearchRequest request) {
        List<Tour> tours = itourRepository.findAll(TourSpecification.searchByCriteria(request));

        return tours.stream().map(tour -> {
            // Map từ entity sang DTO
            TourResponse res = tourMapper.toResponse(tour);

            // Tính và gán rating trung bình từ feedback
            BigDecimal avgRating = feedbackRepository.findAverageRatingByTourId(tour.getTourId());
            res.setRating(avgRating != null ? avgRating.setScale(1, RoundingMode.HALF_UP) : null);

            return res;
        }).toList();
    }

    public List<TourResponse> filterTours(TourFilterRequest filter) {
        return filter.getBaseTours().stream()
                .filter(tour -> {
                    boolean matches = true;

                    if (filter.getMinPrice() != null)
                        matches &= tour.getPrice() != null &&
                                tour.getPrice().compareTo(filter.getMinPrice()) >= 0;

                    if (filter.getMaxPrice() != null)
                        matches &= tour.getPrice() != null &&
                                tour.getPrice().compareTo(filter.getMaxPrice()) <= 0;

                    if (filter.getMinRating() != null)
                        matches &= tour.getRating() != null &&
                                tour.getRating().compareTo(filter.getMinRating()) >= 0;

                    if (filter.getMaxRating() != null)
                        matches &= tour.getRating() != null &&
                                tour.getRating().compareTo(filter.getMaxRating()) <= 0;

                    if (filter.getCreatedByUserName() != null && !filter.getCreatedByUserName().isEmpty())
                        matches &= tour.getCreatedByUserName() != null &&
                                tour.getCreatedByUserName().toLowerCase()
                                        .contains(filter.getCreatedByUserName().toLowerCase());

                    return matches;
                })
                .toList();
    }
}
