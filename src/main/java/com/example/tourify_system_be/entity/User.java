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
@Table(name = "users", schema = "Tourify")
public class User {
    @Id
    @Column(name = "user_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String userId;

    @ColumnDefault("'user'")
    @Column(name = "role")
    String role;

    @Column(name = "user_name")
    String userName;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    @Column(name = "email")
    String email;

    @Column(name = "password")
    String password;

    @Column(name = "phone_number", length = 20)
    String phoneNumber;

    @Column(name = "gender", nullable = false)
    Boolean gender = false;

    @ColumnDefault("'direct'")
    @Column(name = "address")
    String address;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "dob")
    LocalDateTime dob;

    @ColumnDefault("'active'")
    @Column(name = "status")
    String status;

    @Column(name = "social_link")
    String socialLink;

    @Column(name = "avatar")
    String avatar;

    @Column(name = "background")
    String background;

}