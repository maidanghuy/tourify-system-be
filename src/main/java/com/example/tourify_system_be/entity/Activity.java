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
@Table(name = "activity", schema = "Tourify")
public class Activity {
    @Id
    @Column(name = "activity_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String activityId;

    @Column(name = "activity_name")
    String activityName;

    @Column(name = "description")
    String description;

}