package com.example.tourify_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payment_order_refs", schema = "Tourify")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentOrderRef {

    @Id
    @Column(name = "order_code")
    Long orderCode;

    @Column(name = "user_id")
    String userId;

    @Column(name = "booking_id")
    String bookingId;
}
