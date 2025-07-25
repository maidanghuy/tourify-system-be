package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByStatusIgnoreCase(String status);
}

