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
@Table(name = "t_pay", schema = "Tourify")
public class TPay {
    @Id
    @Column(name = "tpay_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String tpayId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ColumnDefault("0.00")
    @Column(name = "balance", nullable = false, precision = 18, scale = 2)
    BigDecimal balance;

    @ColumnDefault("'VND'")
    @Column(name = "currency", nullable = false)
    String currency;

    @ColumnDefault("'active'")
    @Column(name = "status", nullable = false)
    String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

}