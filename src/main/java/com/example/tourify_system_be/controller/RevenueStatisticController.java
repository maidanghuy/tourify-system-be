package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.response.RevenueStatisticResponse;
import com.example.tourify_system_be.service.RevenueStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
public class RevenueStatisticController {

    private final RevenueStatisticService revenueStatisticService;

    /**
     * API: Thống kê doanh thu theo ngày cho 1 sub_company
     * GET /api/revenue/by-day?subCompanyId=...&start=...&end=...
     */
    @GetMapping("/by-day")
    public List<RevenueStatisticResponse> getRevenueByDay(
            @RequestParam String subCompanyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return revenueStatisticService.getRevenueByDay(subCompanyId, start, end);
    }

    /**
     * API: Thống kê doanh thu theo tháng cho 1 sub_company
     * GET /api/revenue/by-month?subCompanyId=...&start=...&end=...
     */
    @GetMapping("/by-month")
    public List<RevenueStatisticResponse> getRevenueByMonth(
            @RequestParam String subCompanyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return revenueStatisticService.getRevenueByMonth(subCompanyId, start, end);
    }

    /**
     * API: Thống kê doanh thu theo năm cho 1 sub_company
     * GET /api/revenue/by-year?subCompanyId=...&start=...&end=...
     */
    @GetMapping("/by-year")
    public List<RevenueStatisticResponse> getRevenueByYear(
            @RequestParam String subCompanyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return revenueStatisticService.getRevenueByYear(subCompanyId, start, end);
    }

    // ============= THỐNG KÊ DOANH THU TOÀN HỆ THỐNG =============

    /**
     * API: Tổng doanh thu toàn hệ thống theo ngày
     * GET /api/revenue/system/by-day?start=...&end=...
     */
    @GetMapping("/system/by-day")
    public List<RevenueStatisticResponse> getTotalRevenueByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return revenueStatisticService.getTotalRevenueByDay(start, end);
    }

    /**
     * API: Tổng doanh thu toàn hệ thống theo tháng
     * GET /api/revenue/system/by-month?start=...&end=...
     */
    @GetMapping("/system/by-month")
    public List<RevenueStatisticResponse> getTotalRevenueByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return revenueStatisticService.getTotalRevenueByMonth(start, end);
    }

    /**
     * API: Tổng doanh thu toàn hệ thống theo năm
     * GET /api/revenue/system/by-year?start=...&end=...
     */
    @GetMapping("/system/by-year")
    public List<RevenueStatisticResponse> getTotalRevenueByYear(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return revenueStatisticService.getTotalRevenueByYear(start, end);
    }
}
