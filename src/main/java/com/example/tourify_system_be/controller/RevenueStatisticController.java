package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.response.RevenueStatisticResponse;
import com.example.tourify_system_be.dto.response.TourBookingStatisticResponse;
import com.example.tourify_system_be.service.RevenueStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
public class RevenueStatisticController {

    private final RevenueStatisticService service;

    @GetMapping("/by-day")
    public List<RevenueStatisticResponse> byDay(
            @RequestParam String subCompanyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getRevenueByDay(subCompanyId, start, end);
    }

    @GetMapping("/by-month")
    public List<RevenueStatisticResponse> byMonth(
            @RequestParam String subCompanyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getRevenueByMonth(subCompanyId, start, end);
    }

    @GetMapping("/by-year")
    public List<RevenueStatisticResponse> byYear(
            @RequestParam String subCompanyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getRevenueByYear(subCompanyId, start, end);
    }

    @GetMapping("/system/by-day")
    public List<RevenueStatisticResponse> sysByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getTotalRevenueByDay(start, end);
    }

    @GetMapping("/system/by-month")
    public List<RevenueStatisticResponse> sysByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getTotalRevenueByMonth(start, end);
    }

    @GetMapping("/system/by-year")
    public List<RevenueStatisticResponse> sysByYear(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getTotalRevenueByYear(start, end);
    }

    @GetMapping("/company/by-day")
    public List<RevenueStatisticResponse> allByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getAllCompaniesRevenueByDay(start, end);
    }

    @GetMapping("/company/by-month")
    public List<RevenueStatisticResponse> allByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getAllCompaniesRevenueByMonth(start, end);
    }

    @GetMapping("/company/by-year")
    public List<RevenueStatisticResponse> allByYear(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getAllCompaniesRevenueByYear(start, end);
    }

    @GetMapping("/top-booked")
    public APIResponse<List<TourBookingStatisticResponse>> topBooked(
            @RequestParam(name = "limit", defaultValue = "5") int limit,
            @RequestParam(name = "subCompanyId", required = false) String subCompanyId) {
        List<TourBookingStatisticResponse> list = service.getMostBookedTours(limit, subCompanyId);
        return APIResponse.<List<TourBookingStatisticResponse>>builder()
                .message("Top " + limit + " booked tours")
                .result(list)
                .build();
    }

    @GetMapping("/active-tours/count")
    public APIResponse<Long> activeTourCount(
            @RequestParam(name = "subCompanyId", required = false) String subCompanyId) {
        long count = service.countActiveTours(subCompanyId);
        return APIResponse.<Long>builder()
                .message("Number of active tours")
                .result(count)
                .build();
    }
}
