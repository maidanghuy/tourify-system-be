package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.TokenAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenAuthenticationRepository extends JpaRepository<TokenAuthentication, String> {
    Optional<TokenAuthentication> findByTokenValue(String tokenValue);
}
