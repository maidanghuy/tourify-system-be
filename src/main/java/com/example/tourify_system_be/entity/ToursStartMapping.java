package com.example.tourify_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tours_start_mapping", schema = "Tourify")
public class ToursStartMapping {
    @EmbeddedId
    ToursStartMappingId id;

    @MapsId("tourId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    Tour tour;

    @MapsId("startId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "start_id", nullable = false)
    ToursStart start;

}