package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.CreatePromotionRequest;

import com.example.tourify_system_be.dto.response.PromotionResponse;
import com.example.tourify_system_be.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    // 2. (Dự phòng) Sẽ thêm editPromotion sau
    // =============================

}
