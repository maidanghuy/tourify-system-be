package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.BookingTour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IBookingTourRepository extends JpaRepository<BookingTour, String> {

    // Thống kê doanh thu của 1 sub_company
    @Query("SELECT FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m-%d'), SUM(b.tour.price) " +
            "FROM BookingTour b " +
            "WHERE b.tour.manageBy.userId = :subCompanyId " +
            "AND b.dayStart BETWEEN :start AND :end " +
            "AND b.status = 'booked' " +
            "GROUP BY FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m-%d') ASC")
    List<Object[]> getRevenueByDay(@Param("subCompanyId") String subCompanyId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m'), SUM(b.tour.price) " +
            "FROM BookingTour b " +
            "WHERE b.tour.manageBy.userId = :subCompanyId " +
            "AND b.dayStart BETWEEN :start AND :end " +
            "AND b.status = 'booked' " +
            "GROUP BY FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m') ASC")
    List<Object[]> getRevenueByMonth(@Param("subCompanyId") String subCompanyId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('YEAR', b.dayStart), SUM(b.tour.price) " +
            "FROM BookingTour b " +
            "WHERE b.tour.manageBy.userId = :subCompanyId " +
            "AND b.dayStart BETWEEN :start AND :end " +
            "AND b.status = 'booked' " +
            "GROUP BY FUNCTION('YEAR', b.dayStart) " +
            "ORDER BY FUNCTION('YEAR', b.dayStart) ASC")
    List<Object[]> getRevenueByYear(@Param("subCompanyId") String subCompanyId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // === Tổng doanh thu toàn hệ thống (không cần subCompanyId) ===

    @Query("SELECT FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m-%d'), SUM(b.tour.price) " +
            "FROM BookingTour b " +
            "WHERE b.dayStart BETWEEN :start AND :end " +
            "AND b.status = 'booked' " +
            "GROUP BY FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m-%d') ASC")
    List<Object[]> getTotalRevenueByDay(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m'), SUM(b.tour.price) " +
            "FROM BookingTour b " +
            "WHERE b.dayStart BETWEEN :start AND :end " +
            "AND b.status = 'booked' " +
            "GROUP BY FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', b.dayStart, '%Y-%m') ASC")
    List<Object[]> getTotalRevenueByMonth(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('YEAR', b.dayStart), SUM(b.tour.price) " +
            "FROM BookingTour b " +
            "WHERE b.dayStart BETWEEN :start AND :end " +
            "AND b.status = 'booked' " +
            "GROUP BY FUNCTION('YEAR', b.dayStart) " +
            "ORDER BY FUNCTION('YEAR', b.dayStart) ASC")
    List<Object[]> getTotalRevenueByYear(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

}
