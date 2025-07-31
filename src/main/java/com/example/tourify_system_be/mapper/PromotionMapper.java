package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.response.PromotionResponse;
import com.example.tourify_system_be.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    @Mapping(source = "createBy.userName", target = "createdByUserName")
    PromotionResponse toPromotionResponse(Promotion promotion);
}
