package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.ToursStart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IToursStartRepository extends JpaRepository<ToursStart, String> {
}
