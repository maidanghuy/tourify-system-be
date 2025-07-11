package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.TokenAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ITokenAuthenticationRepository extends JpaRepository<TokenAuthentication, String> {
    TokenAuthentication findByTokenValue(String tokenValue);
    // Kiểm tra user có token còn hiệu lực không
    boolean existsByUser_UserIdAndIsUsedAndExpiresAtAfter(String userId, Boolean isUsed, LocalDateTime expiresAt);
}