package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.TokenAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITokenAuthenticationRepository extends JpaRepository<TokenAuthentication, String> {
    TokenAuthentication findByTokenValue(String tokenValue);
}