package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.request.PlaceCreateRequest;
import com.example.tourify_system_be.dto.request.PlaceUpdateRequest;
import com.example.tourify_system_be.dto.response.PlaceResponse;
import com.example.tourify_system_be.entity.Place;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlaceMapper {
    PlaceResponse toPlaceResponse(Place place);

    @Mapping(target = "placeId", ignore = true)
    Place toPlace(PlaceCreateRequest request);

    @Mapping(target = "placeId", ignore = true)
    Place toPlace(PlaceUpdateRequest request);
}
