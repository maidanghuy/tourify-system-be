package com.example.tourify_system_be.mapper;


import com.example.tourify_system_be.dto.response.PlaceResponse;
import com.example.tourify_system_be.entity.Place;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlaceMapper {
    PlaceResponse toPlaceResponse(Place place);
}
