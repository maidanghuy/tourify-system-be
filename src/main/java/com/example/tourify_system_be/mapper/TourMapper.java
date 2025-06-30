package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.request.TourCreateRequest;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.entity.TourFavorite;
import org.mapstruct.*;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TourMapper {

    @Mapping(target = "createdByUserName", source = "manageBy.userName")
    @Mapping(target = "placeName", source = "place.placeName")
    @Mapping(target = "categoryName", source = "category.categoryName")
    @Mapping(target = "managementBy", source = "manageBy") // ánh xạ User → UserRespons
    TourResponse toResponse(Tour tour);

    @Mapping(target = "manageBy", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "category", ignore = true)
    Tour toEntity(TourCreateRequest request);


    // MapStruct sẽ gọi mapper này để ánh xạ User → UserResponse
    UserResponse map(User user);
    TourResponse toResponse1(Tour tour);
    @Mapping(target = "createdByUserName", source = "manageBy.userName")
    @Mapping(target = "placeName", source = "place.placeName")
    @Mapping(target = "categoryName", source = "category.categoryName")
    TourResponse toTourResponse(Tour tour);
}
