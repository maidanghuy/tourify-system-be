package com.example.tourify_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaceResponse {
    String placeId;
    String placeName;
    String placeDescription;
    String image;
    String gpsCoordinates;
    BigDecimal rating;
}
