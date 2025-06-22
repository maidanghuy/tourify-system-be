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
@Table(name = "bookings_tour", schema = "Tourify")
public class BookingTour {
    @Id
    @Column(name = "booking_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String bookingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    Tour tour;

    @Column(name = "adult_number", nullable = false)
    Integer adultNumber;

    @Column(name = "child_number", nullable = false)
    Integer childNumber;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "day_start", nullable = false)
    LocalDateTime dayStart;

    @Column(name = "day_end", nullable = false)
    LocalDateTime dayEnd;

    @Column(name = "total_price", nullable = false)
    Integer totalPrice;

    @ColumnDefault("'booked'")
    @Column(name = "status", nullable = false)
    String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

}