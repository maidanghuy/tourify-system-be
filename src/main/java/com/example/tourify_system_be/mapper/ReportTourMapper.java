package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.request.ReportRequest;
import com.example.tourify_system_be.dto.response.ReportResponse;
import com.example.tourify_system_be.entity.ReportTour;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportTourMapper {

    // Map từ ReportRequest + Tour + User => Entity
    @Mapping(target = "reportId", ignore = true)
    @Mapping(target = "tour", source = "tour")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "reasonCode", source = "request.reasonCode")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    ReportTour toEntity(ReportRequest request, Tour tour, User user);

    // Map từ Entity => Response DTO
    @Mapping(target = "tourId", source = "tour.tourId")
    @Mapping(target = "tourName", source = "tour.tourName")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userName", source = "user.userName")
    ReportResponse toDto(ReportTour reportTour);
}
