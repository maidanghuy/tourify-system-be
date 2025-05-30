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
@Table(name = "promotions", schema = "Tourify")
public class Promotion {
    @Id
    @Column(name = "promotion_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String promotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "create_by")
    User createBy;

    @Column(name = "code")
    String code;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "conditions")
    String conditions;

    @Column(name = "discount_percent")
    Integer discountPercent;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "start_time", nullable = false)
    LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    LocalDateTime endTime;

    @ColumnDefault("'active'")
    @Column(name = "status", nullable = false)
    String status;

    @Column(name = "description")
    String description;

    @Column(name = "min_purchase")
    Integer minPurchase;

}