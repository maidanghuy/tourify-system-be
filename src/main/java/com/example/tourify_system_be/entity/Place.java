package com.example.tourify_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "place", schema = "Tourify")
public class Place {
    @Id
    @Column(name = "place_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String placeId;

    @Column(name = "place_name")
    String placeName;

    @Column(name = "place_description")
    String placeDescription;

    @Column(name = "image")
    String image;

    @Column(name = "gps_coordinates")
    String gpsCoordinates;

    @ColumnDefault("5.0")
    @Column(name = "rating", precision = 2, scale = 1)
    BigDecimal rating;

}