package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.ToursStartMapping;
import com.example.tourify_system_be.entity.ToursStartMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IToursStartMappingRepository extends JpaRepository<ToursStartMapping, ToursStartMappingId> {

    /**
     * Lấy startDate (bản active) của tour theo tourId
     */
    @Query("""
   SELECT m.start.startDate
   FROM ToursStartMapping m
   WHERE m.id.tourId = :tourId AND m.start.isActive = true
   ORDER BY m.start.startDate
""")
    List<LocalDateTime> findAllActiveStartDatesByTourId(@Param("tourId") String tourId);

    /**
     * Lấy startDate đầu tiên
     */
    @Query("""
   SELECT MIN(m.start.startDate)
   FROM ToursStartMapping m
   WHERE m.id.tourId = :tourId AND m.start.isActive = true
""")
    Optional<LocalDateTime> findFirstActiveStartDateByTourId(@Param("tourId") String tourId);
}
