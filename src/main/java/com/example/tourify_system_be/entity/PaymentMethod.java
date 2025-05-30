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
@Table(name = "payment_method", schema = "Tourify")
public class PaymentMethod {
    @Id
    @Column(name = "method_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String methodId;

    @Column(name = "method_name")
    String methodName;

    @Column(name = "description")
    String description;

    @ColumnDefault("b'1'")
    @Column(name = "is_active", nullable = false)
    Boolean isActive = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

}