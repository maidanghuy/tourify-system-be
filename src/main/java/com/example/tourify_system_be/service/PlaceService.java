package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.PlaceCreateRequest;
import com.example.tourify_system_be.dto.request.PlaceUpdateRequest;
import com.example.tourify_system_be.dto.response.PageResponse;
import com.example.tourify_system_be.dto.response.PlaceResponse;
import com.example.tourify_system_be.entity.Place;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
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

    public PlaceResponse getPlaceById(String placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND, "Place not found with id: " + placeId));
        return placeMapper.toPlaceResponse(place);
    }

    public PlaceResponse createPlace(PlaceCreateRequest request) {
        Place place = placeMapper.toPlace(request);
        Place savedPlace = placeRepository.save(place);
        return placeMapper.toPlaceResponse(savedPlace);
    }

    public PlaceResponse updatePlace(String placeId, PlaceUpdateRequest request) {
        Place existingPlace = placeRepository.findById(placeId)
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND, "Place not found with id: " + placeId));

        // Update fields
        if (request.getPlaceName() != null) {
            existingPlace.setPlaceName(request.getPlaceName());
        }
        if (request.getPlaceDescription() != null) {
            existingPlace.setPlaceDescription(request.getPlaceDescription());
        }
        if (request.getImage() != null) {
            existingPlace.setImage(request.getImage());
        }
        if (request.getGpsCoordinates() != null) {
            existingPlace.setGpsCoordinates(request.getGpsCoordinates());
        }
        if (request.getRating() != null) {
            existingPlace.setRating(request.getRating());
        }

        Place updatedPlace = placeRepository.save(existingPlace);
        return placeMapper.toPlaceResponse(updatedPlace);
    }

    public void deletePlace(String placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new AppException(ErrorCode.PLACE_NOT_FOUND, "Place not found with id: " + placeId);
        }
        placeRepository.deleteById(placeId);
    }
}
