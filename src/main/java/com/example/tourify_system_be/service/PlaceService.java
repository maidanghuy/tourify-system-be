package com.example.tourify_system_be.service;


import com.example.tourify_system_be.dto.response.PageResponse;
import com.example.tourify_system_be.dto.response.PlaceResponse;
import com.example.tourify_system_be.mapper.PlaceMapper;
import com.example.tourify_system_be.repository.IPlaceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaceService {
    IPlaceRepository placeRepository;
    PlaceMapper placeMapper;

    public List<PlaceResponse> getPlaces() {

        return placeRepository.findAll().stream().map(placeMapper::toPlaceResponse).toList();
    }

    public PageResponse<PlaceResponse> getPlacesWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlaceResponse> placePage = placeRepository.findAll(pageable)
                .map(placeMapper::toPlaceResponse);
        return PageResponse.fromPage(placePage);
    }
}
