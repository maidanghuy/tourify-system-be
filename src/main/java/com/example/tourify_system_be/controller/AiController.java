package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.SuggestTourRequest;
import com.example.tourify_system_be.dto.response.SuggestTourResponse;
import com.example.tourify_system_be.service.SuggestTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final SuggestTourService suggestTourService;

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


}

