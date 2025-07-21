package com.example.tourify_system_be.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class DeletePromotionRequest {
    private List<String> promotionIds;
}
