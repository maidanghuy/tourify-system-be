package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IActivityRepository extends JpaRepository<Activity, String> {
    @Query(value = "SELECT activity_id FROM activity ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<String> findRandomIds(@Param("limit") int limit);
}
