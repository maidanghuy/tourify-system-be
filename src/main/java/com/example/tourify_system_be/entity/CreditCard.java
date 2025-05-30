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
@Table(name = "credit_card", schema = "Tourify")
public class CreditCard {
    @Id
    @Column(name = "cardID", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String cardID;

    @Column(name = "card_number")
    String cardNumber;

    @Column(name = "card_holder_name")
    String cardHolderName;

    @Column(name = "expiry_time")
    LocalDateTime expiryTime;

    @Column(name = "card_type")
    String cardType;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

}