package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.response.AIInsightsResponse;
import com.example.tourify_system_be.dto.response.HotTourResponse;
import com.example.tourify_system_be.repository.IBookingTourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIAnalyticsService {
    private final IBookingTourRepository bookingTourRepo;

    public AIInsightsResponse getInsights(LocalDateTime start, LocalDateTime end, String subCompanyId) {
        // 1. Lấy top 3 tour đặt nhiều nhất kỳ này
        List<Object[]> topTours = subCompanyId == null ?
                bookingTourRepo.findTopBookedTours(PageRequest.of(0, 3)) :
                bookingTourRepo.findTopBookedToursBySubCompanyId(subCompanyId, PageRequest.of(0, 3));

        List<HotTourResponse> hotTourList = new ArrayList<>();
        for (Object[] row : topTours) {
            String tourId = row[0] != null ? row[0].toString() : "";
            String tourName = row[1] != null ? row[1].toString() : "";
            int count = row[2] != null ? ((Number) row[2]).intValue() : 0;
            hotTourList.add(new HotTourResponse(tourId, tourName, count, "Top booked in this period"));
        }

        // 2. So sánh tổng lượt đặt tour kỳ này và kỳ trước (trends)
        LocalDateTime prevStart = start.minusDays(end.toLocalDate().toEpochDay() - start.toLocalDate().toEpochDay() + 1);
        LocalDateTime prevEnd = start.minusDays(1);

        long thisPeriod = 0;
        long prevPeriod = 0;

        // Tổng booking kỳ này
        List<Object[]> currRevenue = (subCompanyId == null)
                ? bookingTourRepo.getTotalRevenueByDay(start, end)
                : bookingTourRepo.getRevenueByDay(subCompanyId, start, end);

        for (Object[] row : currRevenue) {
            thisPeriod += row[1] != null ? ((Number)row[1]).longValue() : 0;
        }

        // Tổng booking kỳ trước
        List<Object[]> prevRevenue = (subCompanyId == null)
                ? bookingTourRepo.getTotalRevenueByDay(prevStart, prevEnd)
                : bookingTourRepo.getRevenueByDay(subCompanyId, prevStart, prevEnd);

        for (Object[] row : prevRevenue) {
            prevPeriod += row[1] != null ? ((Number)row[1]).longValue() : 0;
        }

        double growth = (prevPeriod == 0) ? 0 : ((double)(thisPeriod - prevPeriod) / prevPeriod) * 100.0;
        String trends;
        if (growth > 20)
            trends = "Tour bookings increased strongly (" + String.format("%.1f", growth) + "%) compared to the previous period.";
        else if (growth < -10)
            trends = "Tour bookings decreased (" + String.format("%.1f", growth) + "%) compared to the previous period.";
        else
            trends = "Tour bookings remained stable compared to the previous period.";

        return new AIInsightsResponse(trends, hotTourList);
    }
}

