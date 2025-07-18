package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.CreatePromotionRequest;

import com.example.tourify_system_be.dto.request.UpdatePromotionRequest;
import com.example.tourify_system_be.dto.response.PromotionResponse;
import com.example.tourify_system_be.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    // =============================
    // 1. API: Tạo mới promotion
    // =============================
    @PostMapping
    public APIResponse<?> createPromotion(@RequestBody CreatePromotionRequest request,
                                          @RequestHeader("Authorization") String token) {
        promotionService.createPromotion(request, token);
        return APIResponse.<PromotionResponse>builder()
                .message("Create promotion successfully")
                .result(promotionService.createPromotion(request, token))
                .build();
    }

    // =============================
    // 2. API: Lấy promotion active theo tour
    // =============================
    @GetMapping("/{tourId}")
    public APIResponse<?> getActivePromotionsByTour(@PathVariable String tourId) {
        return APIResponse.<List<PromotionResponse>>builder()
                .message("Danh sách promotion đang active của tour")
                .result(promotionService.getActivePromotionsByTour(tourId))
                .build();
    }

    // =============================
    // 3. API: Edit Promotion
    // =============================
    @PutMapping("/{promotionId}")
    public APIResponse<?> editPromotion(
            @PathVariable String promotionId,
            @RequestBody UpdatePromotionRequest request,
            @RequestHeader("Authorization") String token) {
        PromotionResponse response = promotionService.editPromotion(promotionId, request, token);
        return APIResponse.<PromotionResponse>builder()
                .message("Edit promotion successfully")
                .result(response)
                .build();
    }
}
