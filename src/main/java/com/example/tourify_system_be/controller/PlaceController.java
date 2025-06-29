package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.response.PageResponse;
import com.example.tourify_system_be.dto.response.PlaceResponse;
import com.example.tourify_system_be.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/place")
public class PlaceController {
    @Autowired
    private PlaceService placeService;

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
}
