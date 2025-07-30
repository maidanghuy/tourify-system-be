package com.example.tourify_system_be.dto.request;

import lombok.Data;

@Data
public class SuggestTourRequest {
    private String place;
    private String category;
    private int duration;
}
