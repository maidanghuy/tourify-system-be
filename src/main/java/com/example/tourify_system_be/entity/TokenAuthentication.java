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
@Table(name = "token_authentication", schema = "Tourify")
public class TokenAuthentication {
    @Id
    @Column(name = "token_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String tokenId;

    @Column(name = "token_value")
    String tokenValue;

    @Column(name = "create_at")
    LocalDateTime createAt;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;

    @Column(name = "is_used")
    Boolean isUsed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

}