package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.TourActivity;
import com.example.tourify_system_be.entity.TourActivityId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITourActivityRepository extends JpaRepository<TourActivity, TourActivityId> {}
