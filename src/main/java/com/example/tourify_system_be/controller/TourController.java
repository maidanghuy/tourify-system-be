package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.*;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.TourMapper;
import com.example.tourify_system_be.security.CustomUserDetails;
import com.example.tourify_system_be.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final TourMapper tourMapper;

    @PostMapping("/search")
    public List<TourResponse> searchTours(@RequestBody TourSearchRequest request) {
        return tourService.searchTours(request);
    }

    @PostMapping("/filter")
    public List<TourResponse> filterTours(@RequestBody TourFilterRequest request) {
        return tourService.filterTours(request);
    }

    @PostMapping("")
    public ResponseEntity<APIResponse<?>> createTour(@Valid @RequestBody TourCreateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // hoặc trả về 401
        }

        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        Tour createdTour = tourService.createTour(request, currentUser.getId());
        TourResponse response = tourMapper.toResponse(createdTour);

        return ResponseEntity.ok(APIResponse.builder()
                .code(1000)
                .message("Tour created successfully")
                .result(response)
                .build());
    }


    @GetMapping("/by-place-name")
    public List<TourResponse> getToursByPlaceName(@RequestParam String placeName) {
        return tourService.getToursByPlaceName(placeName);
    }

    @GetMapping("/my-tours")
    public ResponseEntity<?> getMyTours(@RequestHeader("Authorization") String token) {
        List<TourResponse> myTours = tourService.getMyTours(token);
        return ResponseEntity.ok(
                APIResponse.<List<TourResponse>>builder()
                        .code(1000)
                        .message("Success")
                        .result(myTours)
                        .build()
        );
    }

    /**
     * So sánh tour: gửi body ["id1","id2",...], nhận về List<TourResponse]
     */
    @PostMapping("/compare")
    public List<TourResponse> compare(@RequestBody List<String> tourIds) {
        return tourService.compareTours(tourIds);
    }
}
