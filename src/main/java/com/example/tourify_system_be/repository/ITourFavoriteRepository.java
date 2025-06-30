package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.TourFavorite;
import com.example.tourify_system_be.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITourFavoriteRepository extends JpaRepository<TourFavorite, String> {
    List<TourFavorite> findByUser_UserId(String userUserId);

    Optional<TourFavorite> findByUser_UserIdAndTour_TourId(String userId, String tourId);

}