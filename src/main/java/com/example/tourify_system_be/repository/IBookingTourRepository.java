package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.entity.BookingTour;
import com.example.tourify_system_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface IBookingTourRepository extends JpaRepository<BookingTour, String> {

        // 1. Sub-company thống kê theo ngày
        @Query(value = "SELECT d.time, d.totalRevenue, u.user_id, u.user_name " +
                        "FROM users u " +
                        "LEFT JOIN ( " +
                        "  SELECT DATE_FORMAT(b.day_start, '%Y-%m-%d') AS time, " +
                        "         SUM(b.total_price) AS totalRevenue, " +
                        "         t.manage_by AS subCompanyId " +
                        "  FROM bookings_tour b " +
                        "  JOIN tours t ON b.tour_id = t.tour_id " +
                        "  WHERE t.manage_by = :subCompanyId " +
                        "    AND b.day_start BETWEEN :start AND :end " +
                        "    AND b.status = 'SUCCESS' " +
                        "  GROUP BY time, t.manage_by " +
                        ") d ON u.user_id = d.subCompanyId " +
                        "WHERE u.role = 'SUB_COMPANY' AND u.user_id = :subCompanyId " +
                        "ORDER BY d.time", nativeQuery = true)
        List<Object[]> getRevenueByDay(@Param("subCompanyId") String subCompanyId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 2. Sub-company theo tháng
        @Query(value = "SELECT d.time, d.totalRevenue, u.user_id, u.user_name " +
                        "FROM users u " +
                        "LEFT JOIN ( " +
                        "  SELECT DATE_FORMAT(b.day_start, '%Y-%m') AS time, " +
                        "         SUM(b.total_price) AS totalRevenue, " +
                        "         t.manage_by AS subCompanyId " +
                        "  FROM bookings_tour b " +
                        "  JOIN tours t ON b.tour_id = t.tour_id " +
                        "  WHERE t.manage_by = :subCompanyId " +
                        "    AND b.day_start BETWEEN :start AND :end " +
                        "    AND b.status = 'SUCCESS' " +
                        "  GROUP BY time, t.manage_by " +
                        ") d ON u.user_id = d.subCompanyId " +
                        "WHERE u.role = 'SUB_COMPANY' AND u.user_id = :subCompanyId " +
                        "ORDER BY d.time", nativeQuery = true)
        List<Object[]> getRevenueByMonth(@Param("subCompanyId") String subCompanyId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 3. Sub-company theo năm
        @Query(value = "SELECT d.time, d.totalRevenue, u.user_id, u.user_name " +
                        "FROM users u " +
                        "LEFT JOIN ( " +
                        "  SELECT YEAR(b.day_start) AS time, " +
                        "         SUM(b.total_price) AS totalRevenue, " +
                        "         t.manage_by AS subCompanyId " +
                        "  FROM bookings_tour b " +
                        "  JOIN tours t ON b.tour_id = t.tour_id " +
                        "  WHERE t.manage_by = :subCompanyId " +
                        "    AND b.day_start BETWEEN :start AND :end " +
                        "    AND b.status = 'SUCCESS' " +
                        "  GROUP BY time, t.manage_by " +
                        ") d ON u.user_id = d.subCompanyId " +
                        "WHERE u.role = 'SUB_COMPANY' AND u.user_id = :subCompanyId " +
                        "ORDER BY d.time", nativeQuery = true)
        List<Object[]> getRevenueByYear(@Param("subCompanyId") String subCompanyId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 4. Tổng doanh thu hệ thống theo ngày
        @Query(value = "SELECT DATE_FORMAT(b.day_start, '%Y-%m-%d') AS time, " +
                        "       SUM(b.total_price) AS totalRevenue " +
                        "FROM bookings_tour b " +
                        "WHERE b.day_start BETWEEN :start AND :end " +
                        "  AND b.status = 'SUCCESS' " +
                        "GROUP BY time " +
                        "ORDER BY time", nativeQuery = true)
        List<Object[]> getTotalRevenueByDay(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 5. Tổng doanh thu hệ thống theo tháng
        @Query(value = "SELECT DATE_FORMAT(b.day_start, '%Y-%m') AS time, " +
                        "       SUM(b.total_price) AS totalRevenue " +
                        "FROM bookings_tour b " +
                        "WHERE b.day_start BETWEEN :start AND :end " +
                        "  AND b.status = 'SUCCESS' " +
                        "GROUP BY time " +
                        "ORDER BY time", nativeQuery = true)
        List<Object[]> getTotalRevenueByMonth(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 6. Tổng doanh thu hệ thống theo năm
        @Query(value = "SELECT YEAR(b.day_start) AS time, " +
                        "       SUM(b.total_price) AS totalRevenue " +
                        "FROM bookings_tour b " +
                        "WHERE b.day_start BETWEEN :start AND :end " +
                        "  AND b.status = 'SUCCESS' " +
                        "GROUP BY time " +
                        "ORDER BY time", nativeQuery = true)
        List<Object[]> getTotalRevenueByYear(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 7–9. Tất cả SUB_COMPANY: ngày, tháng, năm (trả đủ công ty)
        @Query(value = "SELECT u.user_id, u.user_name, d.time, IFNULL(d.totalRevenue,0) AS totalRevenue " +
                        "FROM users u " +
                        "LEFT JOIN ( " +
                        "  SELECT t.manage_by AS subCompanyId, DATE_FORMAT(b.day_start, '%Y-%m-%d') AS time, " +
                        "         SUM(b.total_price) AS totalRevenue " +
                        "  FROM bookings_tour b JOIN tours t ON b.tour_id = t.tour_id " +
                        "  WHERE b.day_start BETWEEN :start AND :end AND b.status='SUCCESS' " +
                        "  GROUP BY subCompanyId, time " +
                        ") d ON u.user_id = d.subCompanyId " +
                        "WHERE u.role='SUB_COMPANY' " +
                        "ORDER BY u.user_id, d.time", nativeQuery = true)
        List<Object[]> getAllCompaniesRevenueByDay(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query(value = "SELECT u.user_id, u.user_name, d.time, IFNULL(d.totalRevenue,0) AS totalRevenue " +
                        "FROM users u " +
                        "LEFT JOIN ( " +
                        "  SELECT t.manage_by AS subCompanyId, DATE_FORMAT(b.day_start, '%Y-%m') AS time, " +
                        "         SUM(b.total_price) AS totalRevenue " +
                        "  FROM bookings_tour b JOIN tours t ON b.tour_id = t.tour_id " +
                        "  WHERE b.day_start BETWEEN :start AND :end AND b.status='SUCCESS' " +
                        "  GROUP BY subCompanyId, time " +
                        ") d ON u.user_id = d.subCompanyId " +
                        "WHERE u.role='SUB_COMPANY' " +
                        "ORDER BY u.user_id, d.time", nativeQuery = true)
        List<Object[]> getAllCompaniesRevenueByMonth(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query(value = "SELECT u.user_id, u.user_name, d.time, IFNULL(d.totalRevenue,0) AS totalRevenue " +
                        "FROM users u " +
                        "LEFT JOIN ( " +
                        "  SELECT t.manage_by AS subCompanyId, YEAR(b.day_start) AS time, " +
                        "         SUM(b.total_price) AS totalRevenue " +
                        "  FROM bookings_tour b JOIN tours t ON b.tour_id = t.tour_id " +
                        "  WHERE b.day_start BETWEEN :start AND :end AND b.status='SUCCESS' " +
                        "  GROUP BY subCompanyId, time " +
                        ") d ON u.user_id = d.subCompanyId " +
                        "WHERE u.role='SUB_COMPANY' " +
                        "ORDER BY u.user_id, d.time", nativeQuery = true)
        List<Object[]> getAllCompaniesRevenueByYear(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        long countByTour_TourId(String tourId);

        Optional<BookingTour> findByBookingId(String bookingId);

        List<BookingTour> findBookingToursByUser_UserId(String id);

        // Đếm số lượng người dùng khác nhau (distinct users) đã đặt tour mà do người có
        // userId quản lý.
        @Query("SELECT COUNT(DISTINCT b.user.userId) " +
                        "FROM BookingTour b " +
                        "WHERE b.tour.manageBy.userId = :userId")
        int countDistinctUserIdByManageBy(@Param("userId") String userId);

        // Top SUCCESS tours toàn hệ thống
        @Query(value = "SELECT b.tour_id, t.tour_name, COUNT(*) AS cnt " +
                        "FROM bookings_tour b " +
                        "JOIN tours t ON b.tour_id = t.tour_id " +
                        "WHERE b.status = 'SUCCESS' " +
                        "GROUP BY b.tour_id, t.tour_name " +
                        "ORDER BY cnt DESC", nativeQuery = true)
        List<Object[]> findTopBookedTours(Pageable pageable);

        // Top SUCCESS tours của 1 sub-company
        @Query(value = "SELECT b.tour_id, t.tour_name, COUNT(*) AS cnt " +
                        "FROM bookings_tour b " +
                        "JOIN tours t ON b.tour_id = t.tour_id " +
                        "WHERE b.status = 'SUCCESS' AND t.manage_by = :subCompanyId " +
                        "GROUP BY b.tour_id, t.tour_name " +
                        "ORDER BY cnt DESC", nativeQuery = true)
        List<Object[]> findTopBookedToursBySubCompanyId(@Param("subCompanyId") String subCompanyId, Pageable pageable);

        // Tìm tất cả booking của user theo userId
        List<BookingTour> findByUser_UserId(String userId);

        List<BookingTour> findByTour_ManageBy_UserId(String userId);


}
