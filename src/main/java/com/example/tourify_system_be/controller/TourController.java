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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Feedback không hợp lệ và đã bị xoá!"); // hoặc
                                                                                                              // trả về
                                                                                                              // 401
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
    public ResponseEntity<APIResponse<List<TourResponse>>> getMyTours(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        List<TourResponse> myTours = tourService.getAllToursWithDetails(authorizationHeader);

        APIResponse<List<TourResponse>> body = APIResponse.<List<TourResponse>>builder()
                .code(1000)
                .message("Success")
                .result(myTours)
                .build();

        return ResponseEntity.ok(body);
    }

    /**
     * So sánh tour: gửi body ["id1","id2",...], nhận về List<TourResponse]
     */
    @PostMapping(value = "/compare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TourResponse>> compareTours(
            @RequestBody List<String> tourIds) {

        List<TourResponse> comparisons = tourService.compareTours(tourIds);
        return ResponseEntity.ok(comparisons);
    }

    @GetMapping("/{id}")
    public APIResponse<TourResponse> getTourById(@PathVariable("id") String id) {
        TourResponse response = tourService.getTourById(id);
        return APIResponse.<TourResponse>builder()
                .code(1000)
                .result(response)
                .build();
    }

    @GetMapping("")
    public APIResponse<List<TourResponse>> getAllTours() {
        List<TourResponse> response = tourService.getAllTours();
        return APIResponse.<List<TourResponse>>builder()
                .result(response)
                .build();
    }

    /**
     * Lấy danh sách tour theo list id
     */
    @PostMapping("/by-ids")
    public APIResponse<List<TourResponse>> getToursByIds(@RequestBody TourIdsRequest request) {
        List<TourResponse> tours = tourService.getToursByIds(request.getIds());
        return APIResponse.<List<TourResponse>>builder()
                .result(tours)
                .build();
    }

    /**
     * Xóa 1 tour theo ID
     * DELETE /api/tours/{tourId}
     */
    @DeleteMapping("/{tourId}")
    public ResponseEntity<APIResponse<Void>> deleteTour(
            @PathVariable String tourId) {
        tourService.deleteTour(tourId);
        return ResponseEntity.ok(
                APIResponse.<Void>builder()
                        .code(1000)
                        .message("Xóa tour thành công")
                        .build()
        );
    }

    /**
     * GET /api/tours/{id}/start-date
     * Trả về ngày khởi hành active (LocalDateTime)
     */
    @GetMapping("/{id}/start-dates")
    public APIResponse<List<LocalDateTime>> getStartDates(@PathVariable("id") String tourId) {
        List<LocalDateTime> dates = tourService.getAllStartDatesByTourId(tourId);
        return APIResponse.<List<LocalDateTime>>builder()
                .code(1000)
                .message("Success")
                .result(dates)
                .build();
    }

    @PutMapping("/{tourId}/disable")
    public APIResponse<?> disableTour(@PathVariable String tourId) {
        tourService.disableTour(tourId);
        return APIResponse.builder()
                .code(1000)
                .message("Success")
                .result(null)
                .build();
    }

    @PutMapping("/{tourId}")
    public APIResponse<?> updateTour(
            @PathVariable String tourId,
            @RequestBody @Valid TourUpdateRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        tourService.updateTour(tourId, request, bearerToken);
        return new APIResponse<>(1000, "Tour updated successfully", null);
    }

    @GetMapping("/all-draft")
    public ResponseEntity<List<TourResponse>> getAllDraftTours() {
        return ResponseEntity.ok(tourService.getAllDraftTours());
    }

    @PutMapping("/{tourId}/approve")
    public ResponseEntity<?> approveTour(@PathVariable String tourId) {
        tourService.updateStatus(tourId, "ACTIVE");
        return ResponseEntity.ok().body(Map.of("success", true));
    }

}
