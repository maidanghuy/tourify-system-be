package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.TourActivity;
import com.example.tourify_system_be.entity.TourActivityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ITourActivityRepository extends JpaRepository<TourActivity, TourActivityId> {
    List<TourActivity> findByTour_TourId(String tourId);
}
