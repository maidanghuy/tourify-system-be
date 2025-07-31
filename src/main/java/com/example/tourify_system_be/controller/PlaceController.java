package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.PlaceCreateRequest;
import com.example.tourify_system_be.dto.request.PlaceUpdateRequest;
import com.example.tourify_system_be.dto.response.PageResponse;
import com.example.tourify_system_be.dto.response.PlaceResponse;
import com.example.tourify_system_be.security.CustomUserDetails;
import com.example.tourify_system_be.service.PlaceService;
import com.example.tourify_system_be.utils.AuthUtils;
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
    private AuthUtils authUtils;

    @GetMapping("")
    public APIResponse<List<PlaceResponse>> getAllPlaces() {
        return APIResponse.<List<PlaceResponse>>builder()
                .result(placeService.getPlaces())
                .build();
    }

    @GetMapping("/paged")
    public APIResponse<PageResponse<PlaceResponse>> getPlacesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String search) {
        return APIResponse.<PageResponse<PlaceResponse>>builder()
                .result(placeService.getPlacesWithPagination(page, size, search))
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
        // Validate token and check SUB_COMPANY role
        CustomUserDetails userDetails = authUtils.validateAdminToken(token);

        return APIResponse.<PlaceResponse>builder()
                .result(placeService.createPlace(request))
                .build();
    }

    @PutMapping("/{placeId}")
    public APIResponse<PlaceResponse> updatePlace(
            @RequestHeader("Authorization") String token,
            @PathVariable String placeId,
            @RequestBody PlaceUpdateRequest request) {
        // Validate token and check ADMIN role
        CustomUserDetails userDetails = authUtils.validateAdminToken(token);

        return APIResponse.<PlaceResponse>builder()
                .result(placeService.updatePlace(placeId, request))
                .build();
    }

    @DeleteMapping("/{placeId}")
    public APIResponse<Void> deletePlace(
            @RequestHeader("Authorization") String token,
            @PathVariable String placeId) {
        // Validate token and check ADMIN role
        CustomUserDetails userDetails = authUtils.validateAdminToken(token);

        placeService.deletePlace(placeId);
        return APIResponse.<Void>builder()
                .code(1000)
                .message("Place deleted successfully")
                .build();
    }

    @DeleteMapping("/bulk")
    public APIResponse<Void> deletePlaces(
            @RequestHeader("Authorization") String token,
            @RequestBody List<String> placeIds) {
        // Validate token and check ADMIN role
        CustomUserDetails userDetails = authUtils.validateAdminToken(token);

        placeService.deletePlaces(placeIds);
        return APIResponse.<Void>builder()
                .code(1000)
                .message("Places deleted successfully")
                .build();
    }
}