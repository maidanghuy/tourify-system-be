package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.SuggestTourRequest;
import com.example.tourify_system_be.dto.response.AIInsightsResponse;
import com.example.tourify_system_be.dto.response.SuggestTourResponse;
import com.example.tourify_system_be.service.AIAnalyticsService;
import com.example.tourify_system_be.service.SuggestTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final SuggestTourService suggestTourService;
    private final AIAnalyticsService aiAnalyticsService;

    @PostMapping("/suggest-tour")
    public SuggestTourResponse suggestTour(@RequestBody SuggestTourRequest req) {
        return suggestTourService.suggestTour(req);
    }
    @PostMapping("/suggest-tour-from-image")
    public SuggestTourResponse suggestTourFromImage(@RequestParam("image") MultipartFile image) {
        return suggestTourService.suggestTourFromImage(image);
    }

    @PostMapping("/generate-itinerary")
    public ResponseEntity<?> generateItinerary(@RequestBody Map<String, Object> req) {
        String place = (String) req.get("place");
        int duration = (int) req.get("duration");
        List<String> services = (List<String>) req.getOrDefault("services", List.of());

        var result = suggestTourService.generateItineraryAndPrice(place, duration, services);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/parse-excel")
    public ResponseEntity<?> parseExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<Map<String, Object>> tours = suggestTourService.parseExcel(file);
            return ResponseEntity.ok(tours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ai-insights")
    public AIInsightsResponse getAIInsights(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(value = "subCompanyId", required = false) String subCompanyId
    ) {
        return aiAnalyticsService.getInsights(start, end, subCompanyId);
    }
}

