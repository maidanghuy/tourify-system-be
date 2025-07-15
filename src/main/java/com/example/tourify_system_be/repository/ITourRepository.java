package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ITourRepository extends JpaRepository<Tour, String>, JpaSpecificationExecutor<Tour> {
  List<Tour> findAllByManageBy_UserId(String userId);

  // Tìm các tour theo tên địa điểm (placeName) — không phân biệt chữ hoa/thường
  List<Tour> findByPlace_PlaceNameIgnoreCase(String placeName);

  List<Tour> findAllByTourIdIn(List<String> tourIds);

  @Query("SELECT t FROM Tour t WHERE t.tourId = :tourId")
  Optional<Tour> findTourByTourId(@Param("tourId") String tourId);

  Tour findByTourId(String tourId);
}
