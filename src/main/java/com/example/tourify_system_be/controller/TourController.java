package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.TourFilterRequest;
import com.example.tourify_system_be.dto.request.TourSearchRequest;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @PostMapping("/search")
    public List<TourResponse> searchTours(@RequestBody TourSearchRequest request) {
        return tourService.searchTours(request);
    }

    @PostMapping("/filter")
    public List<TourResponse> filterTours(@RequestBody TourFilterRequest request) {
        return tourService.filterTours(request);
    }

}
