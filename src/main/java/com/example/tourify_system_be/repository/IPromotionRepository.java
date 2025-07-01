package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPromotionRepository extends JpaRepository<Promotion, String> {
    Optional<Promotion> findByCode(String code);
    List<Promotion> findAllByStatus(String status);
}
