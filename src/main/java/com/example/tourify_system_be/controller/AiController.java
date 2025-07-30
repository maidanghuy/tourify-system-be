package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.SuggestTourRequest;
import com.example.tourify_system_be.dto.response.SuggestTourResponse;
import com.example.tourify_system_be.service.SuggestTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final SuggestTourService suggestTourService;

    @PostMapping("/suggest-tour")
    public SuggestTourResponse suggestTour(@RequestBody SuggestTourRequest req) {
        return suggestTourService.suggestTour(req);
    }
}

