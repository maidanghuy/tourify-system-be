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
    String status; // SUCCESS, FAILED, PENDING...

    @Column(name = "amount", precision = 10, scale = 2)
    BigDecimal amount;

    @Column(name = "pay_at")
    LocalDateTime payAt;

    @Column(name = "payment_reference")
    String paymentReference; // FT... từ PayOS

    @Column(name = "order_code")
    Long orderCode; // để đối chiếu từ webhook

    @Column(name = "payer_name")
    String payerName; // MAI DANG HUY

    @Column(name = "payer_account_number")
    String payerAccountNumber;

    @Column(name = "currency")
    String currency; // VND

}
