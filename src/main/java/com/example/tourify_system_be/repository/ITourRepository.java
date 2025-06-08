package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ITourRepository extends JpaRepository<Tour, String>, JpaSpecificationExecutor<Tour> {
}
