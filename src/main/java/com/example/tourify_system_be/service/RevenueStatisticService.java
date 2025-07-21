package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.response.RevenueStatisticResponse;
import com.example.tourify_system_be.dto.response.TourBookingStatisticResponse;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.repository.IBookingTourRepository;
import com.example.tourify_system_be.repository.ITourRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class RevenueStatisticService {

    private final IBookingTourRepository bookingTourRepository;
    private final IUserRepository userRepository;
    private final ITourRepository tourRepo;

    private static final DateTimeFormatter DAY_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    public List<RevenueStatisticResponse> getRevenueByDay(String subCompanyId, LocalDate start, LocalDate end) {
        // Lấy dữ liệu DB (time là yyyy-MM-dd)
        List<Object[]> raw = bookingTourRepository.getRevenueByDay(
                subCompanyId, start.atStartOfDay(), end.plusDays(1).atStartOfDay().minusNanos(1));
        Map<String, Long> map = toMap(raw);

        String name = userRepository.findById(subCompanyId)
                .map(User::getUserName)
                .orElse("");

        List<RevenueStatisticResponse> out = new ArrayList<>();
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            String key = cur.format(DAY_FMT);
            out.add(new RevenueStatisticResponse(key, map.getOrDefault(key, 0L),
                    subCompanyId, name));
            cur = cur.plusDays(1);
        }
        return out;
    }

    public List<RevenueStatisticResponse> getRevenueByMonth(String subCompanyId, LocalDate start, LocalDate end) {
        List<Object[]> raw = bookingTourRepository.getRevenueByMonth(
                subCompanyId, start.withDayOfMonth(1).atStartOfDay(),
                end.withDayOfMonth(end.lengthOfMonth()).plusDays(1).atStartOfDay().minusNanos(1));
        Map<String, Long> map = toMap(raw);

        String name = userRepository.findById(subCompanyId)
                .map(User::getUserName)
                .orElse("");

        List<RevenueStatisticResponse> out = new ArrayList<>();
        LocalDate cur = start.withDayOfMonth(1);
        while (!cur.isAfter(end)) {
            String key = cur.format(MONTH_FMT);
            out.add(new RevenueStatisticResponse(key, map.getOrDefault(key, 0L),
                    subCompanyId, name));
            cur = cur.plusMonths(1);
        }
        return out;
    }

    public List<RevenueStatisticResponse> getRevenueByYear(String subCompanyId, LocalDate start, LocalDate end) {
        List<Object[]> raw = bookingTourRepository.getRevenueByYear(
                subCompanyId, start.atStartOfDay(), end.plusDays(1).atStartOfDay().minusNanos(1));
        Map<String, Long> map = toMap(raw);

        String name = userRepository.findById(subCompanyId)
                .map(User::getUserName)
                .orElse("");

        List<RevenueStatisticResponse> out = new ArrayList<>();
        for (int y = start.getYear(); y <= end.getYear(); y++) {
            String key = String.valueOf(y);
            out.add(new RevenueStatisticResponse(key, map.getOrDefault(key, 0L),
                    subCompanyId, name));
        }
        return out;
    }

    public List<RevenueStatisticResponse> getTotalRevenueByDay(LocalDate start, LocalDate end) {
        List<Object[]> raw = bookingTourRepository.getTotalRevenueByDay(
                start.atStartOfDay(), end.plusDays(1).atStartOfDay().minusNanos(1));
        Map<String, Long> map = toMap(raw);

        List<RevenueStatisticResponse> out = new ArrayList<>();
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            String key = cur.format(DAY_FMT);
            out.add(new RevenueStatisticResponse(key, map.getOrDefault(key, 0L), null, null));
            cur = cur.plusDays(1);
        }
        return out;
    }

    public List<RevenueStatisticResponse> getTotalRevenueByMonth(LocalDate start, LocalDate end) {
        List<Object[]> raw = bookingTourRepository.getTotalRevenueByMonth(
                start.withDayOfMonth(1).atStartOfDay(),
                end.withDayOfMonth(end.lengthOfMonth()).plusDays(1).atStartOfDay().minusNanos(1));
        Map<String, Long> map = toMap(raw);

        List<RevenueStatisticResponse> out = new ArrayList<>();
        LocalDate cur = start.withDayOfMonth(1);
        while (!cur.isAfter(end)) {
            String key = cur.format(MONTH_FMT);
            out.add(new RevenueStatisticResponse(key, map.getOrDefault(key, 0L), null, null));
            cur = cur.plusMonths(1);
        }
        return out;
    }

    public List<RevenueStatisticResponse> getTotalRevenueByYear(LocalDate start, LocalDate end) {
        List<Object[]> raw = bookingTourRepository.getTotalRevenueByYear(
                start.atStartOfDay(), end.plusDays(1).atStartOfDay().minusNanos(1));
        Map<String, Long> map = toMap(raw);

        List<RevenueStatisticResponse> out = new ArrayList<>();
        for (int y = start.getYear(); y <= end.getYear(); y++) {
            String key = String.valueOf(y);
            out.add(new RevenueStatisticResponse(key, map.getOrDefault(key, 0L), null, null));
        }
        return out;
    }

    public List<RevenueStatisticResponse> getAllCompaniesRevenueByDay(LocalDate start, LocalDate end) {
        return allCompanies(start, end, "day");
    }
    public List<RevenueStatisticResponse> getAllCompaniesRevenueByMonth(LocalDate start, LocalDate end) {
        return allCompanies(start, end, "month");
    }
    public List<RevenueStatisticResponse> getAllCompaniesRevenueByYear(LocalDate start, LocalDate end) {
        return allCompanies(start, end, "year");
    }

    // chung giải thuật cho tất cả sub-company
    private List<RevenueStatisticResponse> allCompanies(LocalDate start, LocalDate end, String period) {
        List<User> subs = userRepository.findAllByRole("SUB_COMPANY");
        List<Object[]> raw;
        Set<String> keys = new LinkedHashSet<>();

        if (period.equals("day")) {
            raw = bookingTourRepository.getAllCompaniesRevenueByDay(
                    start.atStartOfDay(), end.plusDays(1).atStartOfDay().minusNanos(1));
            LocalDate cur = start;
            while (!cur.isAfter(end)) { keys.add(cur.format(DAY_FMT)); cur = cur.plusDays(1); }
        } else if (period.equals("month")) {
            raw = bookingTourRepository.getAllCompaniesRevenueByMonth(
                    start.withDayOfMonth(1).atStartOfDay(),
                    end.withDayOfMonth(end.lengthOfMonth()).plusDays(1).atStartOfDay().minusNanos(1));
            LocalDate cur = start.withDayOfMonth(1);
            while (!cur.isAfter(end)) { keys.add(cur.format(MONTH_FMT)); cur = cur.plusMonths(1); }
        } else {
            raw = bookingTourRepository.getAllCompaniesRevenueByYear(
                    start.atStartOfDay(), end.plusDays(1).atStartOfDay().minusNanos(1));
            for (int y = start.getYear(); y <= end.getYear(); y++) { keys.add(String.valueOf(y)); }
        }

        Map<String, RevenueStatisticResponse> map = new HashMap<>();
        for (Object[] r : raw) {
            String cid  = String.valueOf(r[0]);
            String name = String.valueOf(r[1]);
            String t    = String.valueOf(r[2]);
            Long rev    = r[3]==null?0L:((Number)r[3]).longValue();
            map.put(cid+"_"+t, new RevenueStatisticResponse(t, rev, cid, name));
        }

        List<RevenueStatisticResponse> out = new ArrayList<>();
        for (User u : subs) {
            for (String k : keys) {
                out.add(map.getOrDefault(u.getUserId()+"_"+k,
                        new RevenueStatisticResponse(k, 0L, u.getUserId(), u.getUserName())));
            }
        }
        return out;
    }

    // helper
    private Map<String, Long> toMap(List<Object[]> raw) {
        Map<String, Long> m = new HashMap<>();
        for (Object[] r : raw) {
            String t = String.valueOf(r[0]);
            Long v    = r[1] == null ? 0L : ((Number) r[1]).longValue();
            if (!"null".equals(t)) m.put(t, v);
        }
        return m;
    }

    public List<TourBookingStatisticResponse> getMostBookedTours(int topN, String subCompanyId) {
        Pageable limit = PageRequest.of(0, topN);
        List<Object[]> raw;
        if (subCompanyId != null && !subCompanyId.isEmpty()) {
            // Chỉ lấy top tour của sub-company đó
            raw = bookingTourRepository.findTopBookedToursBySubCompanyId(subCompanyId, limit);
        } else {
            // Lấy top tour toàn hệ thống
            raw = bookingTourRepository.findTopBookedTours(limit);
        }
        return raw.stream()
                .map(r -> new TourBookingStatisticResponse(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public long countActiveTours(String subCompanyId) {
        if (subCompanyId != null && !subCompanyId.isEmpty()) {
            // Đếm tour active của 1 sub-company (role: SUB_COMPANY)
            return tourRepo.countByStatusIgnoreCaseAndManageBy_UserIdIgnoreCase("active", subCompanyId);
        } else {
            // Đếm tất cả tour active (role: ADMIN)
            return tourRepo.countByStatusIgnoreCase("active");
        }
    }
}
