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
@Table(name = "permissions", schema = "Tourify")
public class Permission {
    @Id
    @Column(name = "permission_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String permissionId;

    @Column(name = "permission_name")
    String permissionName;

    @Column(name = "description")
    String description;

    @Column(name = "level")
    Integer level;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_at")
    LocalDateTime createAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "update_at")
    LocalDateTime updateAt;

}