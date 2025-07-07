package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.response.PromotionResponse;
import com.example.tourify_system_be.entity.Promotion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    PromotionResponse toPromotionResponse(Promotion promotion);
}
