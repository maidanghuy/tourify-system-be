package com.example.tourify_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "services", schema = "Tourify")
public class Service {
    @Id
    @Column(name = "service_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String serviceId;

    @Column(name = "service_name")
    String serviceName;

    @Column(name = "description")
    String description;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

}