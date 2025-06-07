package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.entity.Tour;
import org.mapstruct.*;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TourMapper {

    @Mapping(target = "createdByUserName", source = "manageBy.userName")
    @Mapping(target = "placeName", source = "place.placeName")
    @Mapping(target = "categoryName", source = "category.categoryName")
    TourResponse toResponse(Tour tour);
}
