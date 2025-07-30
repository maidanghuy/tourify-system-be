package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IServicesRepository extends JpaRepository<Services, String> {
    @Query(value = "SELECT service_id FROM services ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<String> findRandomIds(@Param("limit") int limit);
}
