package com.example.tourify_system_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuggestTourResponse {
    String place;
    String category;
    private String tourName;
    private String description;
    private double price;
    private List<String> serviceIds;
    private List<String> activityIds;
}
