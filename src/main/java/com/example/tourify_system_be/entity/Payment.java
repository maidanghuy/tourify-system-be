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
@Table(name = "payments", schema = "Tourify")
public class Payment {
    @Id
    @Column(name = "payment_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    BookingTour booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_method_id", nullable = false)
    PaymentMethod paymentMethod;

    @Column(name = "status")
    String status;

    @Column(name = "amount", precision = 10, scale = 2)
    BigDecimal amount;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "pay_at", nullable = false)
    LocalDateTime payAt;

    @Column(name = "payment_reference")
    String paymentReference;

}