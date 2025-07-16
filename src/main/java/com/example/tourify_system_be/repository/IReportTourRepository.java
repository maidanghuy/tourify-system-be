package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.ReportTour;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IReportTourRepository extends JpaRepository<ReportTour, Long> {
    Optional<ReportTour> findByTour_TourIdAndUser_UserId(String tourId, String userId);
    boolean existsByTour_TourIdAndUser_UserId(String tourId, String userId);
}
