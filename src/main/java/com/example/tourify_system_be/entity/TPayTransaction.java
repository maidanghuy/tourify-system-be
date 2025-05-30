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
@Table(name = "t_pay_transaction", schema = "Tourify")
public class TPayTransaction {
    @Id
    @Column(name = "transaction_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tpay_id", nullable = false)
    TPay tpay;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    BigDecimal amount;

    @Column(name = "type", nullable = false)
    String type;

    @ColumnDefault("'pending'")
    @Column(name = "status", nullable = false)
    String status;

    @Column(name = "description")
    String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

}