package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.entity.BookingTour;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface TourBookingMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "tourId", source = "tour.tourId")
    @Mapping(target = "tourName", source = "tour.tourName")
    @Mapping(target = "thumbnail", source = "tour.thumbnail")
    BookingTourResponse toBookingTourResponse(BookingTour bookingTour);
}
