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
@Table(name = "tour_waitlist", schema = "Tourify")
public class TourWaitlist {
    @Id
    @Column(name = "waitlist_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String waitlistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    Tour tour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "register_date", nullable = false)
    LocalDateTime registerDate;

    @Column(name = "status", nullable = false, length = 20)
    String status;

    @Column(name = "expiration_date")
    LocalDateTime expirationDate;

    @ColumnDefault("1")
    @Column(name = "adult_number", nullable = false)
    Integer adultNumber;

    @ColumnDefault("0")
    @Column(name = "child_number", nullable = false)
    Integer childNumber;

}