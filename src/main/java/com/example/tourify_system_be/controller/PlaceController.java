package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.PlaceCreateRequest;
import com.example.tourify_system_be.dto.request.PlaceUpdateRequest;
import com.example.tourify_system_be.dto.response.PageResponse;
import com.example.tourify_system_be.dto.response.PlaceResponse;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.security.CustomUserDetails;
import com.example.tourify_system_be.security.JwtUtil;
import com.example.tourify_system_be.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/place")
public class PlaceController {
    @Autowired
    private PlaceService placeService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("")
    public APIResponse<List<PlaceResponse>> getAllPlaces() {
        return APIResponse.<List<PlaceResponse>>builder()
                .result(placeService.getPlaces())
                .build();
    }

    @GetMapping("/paged")
    public APIResponse<PageResponse<PlaceResponse>> getPlacesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return APIResponse.<PageResponse<PlaceResponse>>builder()
                .result(placeService.getPlacesWithPagination(page, size))
                .build();
    }

    @GetMapping("/{placeId}")
    public APIResponse<PlaceResponse> getPlaceById(@PathVariable String placeId) {
        return APIResponse.<PlaceResponse>builder()
                .result(placeService.getPlaceById(placeId))
                .build();
    }

    @PostMapping("")
    public APIResponse<PlaceResponse> createPlace(
            @RequestHeader("Authorization") String token,
            @RequestBody PlaceCreateRequest request) {
        // Validate token and check role
        validateTokenAndRole(token);

        return APIResponse.<PlaceResponse>builder()
                .result(placeService.createPlace(request))
                .build();
    }

    @PutMapping("/{placeId}")
    public APIResponse<PlaceResponse> updatePlace(
            @RequestHeader("Authorization") String token,
            @PathVariable String placeId,
            @RequestBody PlaceUpdateRequest request) {
        // Validate token and check role
        validateTokenAndRole(token);

        return APIResponse.<PlaceResponse>builder()
                .result(placeService.updatePlace(placeId, request))
                .build();
    }

    @DeleteMapping("/{placeId}")
    public APIResponse<Void> deletePlace(
            @RequestHeader("Authorization") String token,
            @PathVariable String placeId) {
        // Validate token and check role
        validateTokenAndRole(token);

        placeService.deletePlace(placeId);
        return APIResponse.<Void>builder()
                .code(1000)
                .message("Place deleted successfully")
                .build();
    }

    /**
     * Validate JWT token and check if user has SUB_COMPANY role
     */
    private void validateTokenAndRole(String token) {
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid token");
        }

        // Extract user details from token
        CustomUserDetails userDetails = jwtUtil.getUserDetailsFromToken(token);

        // Check if user has SUB_COMPANY role
        if (!"SUB_COMPANY".equals(userDetails.getRole())) {
            throw new AppException(ErrorCode.ROLE_ALLOWED_TOUR_COMPANY,
                    "Only SUB_COMPANY role is allowed to perform this operation");
        }
    }
}
