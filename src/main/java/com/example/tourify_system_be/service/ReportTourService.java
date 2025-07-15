package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.ReportRequest;
import com.example.tourify_system_be.dto.response.ReportResponse;
import com.example.tourify_system_be.entity.ReportTour;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.ReportTourMapper;
import com.example.tourify_system_be.repository.IReportTourRepository;
import com.example.tourify_system_be.repository.ITourRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportTourService {

    private final IReportTourRepository reportTourRepository;
    private final ITourRepository tourRepository;
    private final IUserRepository userRepository;
    private final ReportTourMapper reportTourMapper;

    private static final Set<String> ALLOWED_REASON_CODES = Set.of("INAPPROPRIATE", "FRAUD", "OTHER");
    private static final List<String> BANNED_WORDS = Arrays.asList("đmm", "đm", "cc", "cặc", "lồn", "fuck", "shit");

    @Transactional
    public ReportResponse createReport(ReportRequest request) {
        // userId trong request đã được controller set rồi
        String userId = request.getUserId();

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND, "Tour not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String role = user.getRole();
        if (role == null || !(role.equalsIgnoreCase("USER") || role.equalsIgnoreCase("ADMIN"))) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED, "Role not allowed");
        }

        if (reportTourRepository.existsByTour_TourIdAndUser_UserId(request.getTourId(), userId)) {
            throw new AppException(ErrorCode.TOUR_ALREADY_REPORTED, "Already reported");
        }

        if (request.getReasonCode() == null || !ALLOWED_REASON_CODES.contains(request.getReasonCode().trim().toUpperCase())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid reason code");
        }

        if (request.getDescription() != null) {
            String descLower = request.getDescription().toLowerCase();
            for (String banned : BANNED_WORDS) {
                if (descLower.contains(banned)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST, "Description contains inappropriate language");
                }
            }
        }

        ReportTour reportTour = reportTourMapper.toEntity(request, tour, user);
        ReportTour savedReport = reportTourRepository.save(reportTour);
        return reportTourMapper.toDto(savedReport);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports(int page, int size) {
        Page<ReportTour> reports = reportTourRepository.findAll(PageRequest.of(page, size));
        return reports.stream().map(reportTourMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId, String adminUserId) {
        ReportTour report = reportTourRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Report not found"));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Admin not found"));

        if (admin.getRole() == null || !admin.getRole().equalsIgnoreCase("ADMIN")) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED, "Only ADMIN can resolve");
        }

        report.setStatus("RESOLVED");
        ReportTour saved = reportTourRepository.save(report);
        return reportTourMapper.toDto(saved);
    }

    @Transactional
    public void deleteReport(Long reportId, String adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Admin not found"));

        if (admin.getRole() == null || !admin.getRole().equalsIgnoreCase("ADMIN")) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED, "Only ADMIN can delete report");
        }

        if (!reportTourRepository.existsById(reportId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Report not found");
        }

        reportTourRepository.deleteById(reportId);
    }

    @Transactional
    public void cancelReport(String tourId, String userId) {
        ReportTour report = reportTourRepository.findByTour_TourIdAndUser_UserId(tourId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "You haven't reported this tour"));
        reportTourRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByUser(String userId) {
        List<ReportTour> reports = reportTourRepository.findAll()
                .stream()
                .filter(r -> r.getUser().getUserId().equals(userId))
                .collect(Collectors.toList());
        return reports.stream().map(reportTourMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasReported(String tourId, String userId) {
        return reportTourRepository.existsByTour_TourIdAndUser_UserId(tourId, userId);
    }
}
