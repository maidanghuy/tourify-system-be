package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.TourPromotion;
import com.example.tourify_system_be.entity.TourPromotionId;
import com.example.tourify_system_be.entity.Promotion;
import com.example.tourify_system_be.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ITourPromotionRepository extends JpaRepository<TourPromotion, TourPromotionId> {

    // Tìm tất cả các tour được áp dụng theo promotion
    List<TourPromotion> findByPromotion(Promotion promotion);

    // Tìm tất cả promotion áp dụng cho 1 tour cụ thể
    List<TourPromotion> findByTour(Tour tour);

    // Xoá toàn bộ các dòng liên kết theo promotion
    void deleteByPromotion(Promotion promotion);

    // Kiểm tra tồn tại 1 liên kết cụ thể
    Optional<TourPromotion> findById(TourPromotionId id);

    boolean existsById(TourPromotionId id);
}

