package com.example.tourify_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tours_start", schema = "Tourify")
public class ToursStart {
    @Id
    @Column(name = "start_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String startId;

    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;

    @ColumnDefault("b'1'")
    @Column(name = "is_active")
    Boolean isActive;

}