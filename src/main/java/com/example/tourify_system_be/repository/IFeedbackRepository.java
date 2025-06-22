package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IFeedbackRepository extends JpaRepository<Feedback, String> {

    // Lấy danh sách feedback theo tour
    List<Feedback> findByTour_TourId(String tourId);

    // Lấy danh sách feedback theo tour và status
    List<Feedback> findByTour_TourIdAndStatus(String tourId, String status);

    // Tính trung bình rating (tối ưu)
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.tour.tourId = :tourId")
    BigDecimal findAverageRatingByTourId(@Param("tourId") String tourId);
}
