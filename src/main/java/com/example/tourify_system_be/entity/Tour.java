package com.example.tourify_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tours", schema = "Tourify")
public class Tour {
    @Id
    @Column(name = "tour_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String tourId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manage_by")
    User manageBy;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "duration")
    Integer duration;

    @Column(name = "description")
    String description;

    @Column(name = "price", precision = 10, scale = 2)
    BigDecimal price;

    @Column(name = "tourist_number_assigned")
    Integer touristNumberAssigned;

    @Column(name = "min_people")
    Integer minPeople;

    @Column(name = "max_people")
    Integer maxPeople;

    @Column(name = "tour_name")
    String tourName;

    @Column(name = "thumbnail")
    String thumbnail;

    @ColumnDefault("'active'")
    @Column(name = "status")
    String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

}