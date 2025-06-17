package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.TourCreateRequest;
import com.example.tourify_system_be.dto.request.TourFilterRequest;
import com.example.tourify_system_be.dto.request.TourSearchRequest;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.entity.Category;
import com.example.tourify_system_be.entity.Place;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.TourMapper;
import com.example.tourify_system_be.repository.*;
import com.example.tourify_system_be.specification.TourSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourService {

    private final ITourRepository itourRepository;
    private final FeedbackRepository feedbackRepository;
    private final TourMapper tourMapper;
    private final IUserRepository iUserRepository;
    private final IPlaceRepository iPlaceRepository;
    private final ICategoryRepository iCategoryRepository;

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

    public Tour createTour(TourCreateRequest request, String userId) {
        Tour tour = tourMapper.toEntity(request);

        // Lấy thông tin người dùng từ userId lấy từ token
        User creator = iUserRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Chỉ cho phép role SUB_COMPANY tạo tour
        if (!"SUB_COMPANY".equalsIgnoreCase(creator.getRole())) {
            throw new AppException(ErrorCode.NOT_SUBCOMPANY);
        }

        // Tìm place và category
        Place place = iPlaceRepository.findById(request.getPlace())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        Category category = iCategoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Gán entity cho tour
        tour.setManageBy(creator);
        tour.setPlace(place);
        tour.setCategory(category);
        tour.setCreatedAt(LocalDateTime.now());
        tour.setUpdatedAt(LocalDateTime.now());

        return itourRepository.save(tour);
    }
}
