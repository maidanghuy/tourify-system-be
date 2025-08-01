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
import com.example.tourify_system_be.repository.ITourRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaceService {
    IPlaceRepository placeRepository;
    ITourRepository tourRepository;
    PlaceMapper placeMapper;

    public List<PlaceResponse> getPlaces() {
        return placeRepository.findAll().stream().map(placeMapper::toPlaceResponse).toList();
    }

    public PageResponse<PlaceResponse> getPlacesWithPagination(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlaceResponse> placePage;

        if (search != null && !search.trim().isEmpty()) {
            // Search by place name or description
            placePage = placeRepository.findByPlaceNameContainingIgnoreCaseOrPlaceDescriptionContainingIgnoreCase(
                    search.trim(), search.trim(), pageable)
                    .map(placeMapper::toPlaceResponse);
        } else {
            placePage = placeRepository.findAll(pageable)
                    .map(placeMapper::toPlaceResponse);
        }

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

    @Transactional
    public void deletePlaces(List<String> placeIds) {
        if (placeIds == null || placeIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "No place IDs provided");
        }

        // Check if all places exist
        List<Place> placesToDelete = placeRepository.findAllById(placeIds);
        if (placesToDelete.size() != placeIds.size()) {
            throw new AppException(ErrorCode.PLACE_NOT_FOUND, "Some places not found");
        }

        // Kiểm tra từng place có tour nào không
        for (String placeId : placeIds) {
            boolean linked = tourRepository.existsByPlace_PlaceId(placeId);
            if (linked) {
                throw new AppException(
                        ErrorCode.PLACE_HAS_TOUR,
                        "Không thể xóa địa điểm vì đã có tour liên kết: " + placeId
                );
            }
        }

        // Không có tour liên quan => xóa
        placeRepository.deleteAllById(placeIds);
    }

}
