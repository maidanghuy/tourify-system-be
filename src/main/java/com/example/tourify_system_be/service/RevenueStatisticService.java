package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.response.RevenueStatisticResponse;
import com.example.tourify_system_be.repository.IBookingTourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueStatisticService {

    private final IBookingTourRepository bookingTourRepository;

    // ==== Theo từng sub_company ====

    /** Thống kê doanh thu theo ngày cho 1 sub_company */
    public List<RevenueStatisticResponse> getRevenueByDay(String subCompanyId, LocalDateTime start, LocalDateTime end) {
        return bookingTourRepository.getRevenueByDay(subCompanyId, start, end)
                .stream()
                .map(obj -> new RevenueStatisticResponse(
                        String.valueOf(obj[0]),
                        obj[1] == null ? 0L : ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /** Thống kê doanh thu theo tháng cho 1 sub_company */
    public List<RevenueStatisticResponse> getRevenueByMonth(String subCompanyId, LocalDateTime start, LocalDateTime end) {
        return bookingTourRepository.getRevenueByMonth(subCompanyId, start, end)
                .stream()
                .map(obj -> new RevenueStatisticResponse(
                        String.valueOf(obj[0]),
                        obj[1] == null ? 0L : ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /** Thống kê doanh thu theo năm cho 1 sub_company */
    public List<RevenueStatisticResponse> getRevenueByYear(String subCompanyId, LocalDateTime start, LocalDateTime end) {
        return bookingTourRepository.getRevenueByYear(subCompanyId, start, end)
                .stream()
                .map(obj -> new RevenueStatisticResponse(
                        String.valueOf(obj[0]),
                        obj[1] == null ? 0L : ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    // ==== Tổng doanh thu toàn hệ thống ====

    /** Tổng doanh thu hệ thống theo ngày */
    public List<RevenueStatisticResponse> getTotalRevenueByDay(LocalDateTime start, LocalDateTime end) {
        return bookingTourRepository.getTotalRevenueByDay(start, end)
                .stream()
                .map(obj -> new RevenueStatisticResponse(
                        String.valueOf(obj[0]),
                        obj[1] == null ? 0L : ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /** Tổng doanh thu hệ thống theo tháng */
    public List<RevenueStatisticResponse> getTotalRevenueByMonth(LocalDateTime start, LocalDateTime end) {
        return bookingTourRepository.getTotalRevenueByMonth(start, end)
                .stream()
                .map(obj -> new RevenueStatisticResponse(
                        String.valueOf(obj[0]),
                        obj[1] == null ? 0L : ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /** Tổng doanh thu hệ thống theo năm */
    public List<RevenueStatisticResponse> getTotalRevenueByYear(LocalDateTime start, LocalDateTime end) {
        return bookingTourRepository.getTotalRevenueByYear(start, end)
                .stream()
                .map(obj -> new RevenueStatisticResponse(
                        String.valueOf(obj[0]),
                        obj[1] == null ? 0L : ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }
}
