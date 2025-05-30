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
@Table(name = "image", schema = "Tourify")
public class Image {
    @Id
    @Column(name = "image_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String imageId;

    @Column(name = "image_link")
    String imageLink;

    @Column(name = "description")
    String description;

    @ColumnDefault("1")
    @Column(name = "image_order")
    Integer imageOrder;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_at")
    LocalDateTime createAt;

}