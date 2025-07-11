package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IFeedbackRepository extends JpaRepository<Feedback, String> {

    /** Lấy tất cả feedback theo tour */
    List<Feedback> findByTour_TourId(String tourId);

    List<Feedback> findByTour_TourIdAndStatus(String tourId, String status);

    /**
     * Tính trung bình rating (tối ưu)
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.tour.tourId = :tourId")
    BigDecimal findAverageRatingByTourId(@Param("tourId") String tourId);

    Optional<Feedback> findTopByTour_TourIdAndStatusOrderByCreateAtDesc(String tourId, String status);
    Optional<Feedback> findTopByTour_TourIdOrderByCreateAtDesc(String tourId);
    void deleteByTour_TourId(String tourId);
}
