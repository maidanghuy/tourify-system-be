package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.ReportRequest;
import com.example.tourify_system_be.dto.response.ReportResponse;
import com.example.tourify_system_be.service.ReportTourService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/report-tours")
@RequiredArgsConstructor
public class ReportController {

    private final ReportTourService reportTourService;

    // Thay bằng secret key thực tế của bạn (>= 32 ký tự)
    private static final String SECRET_KEY = "your-very-strong-secret-key-1234567890";

    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // Giải mã JWT lấy userId từ header Authorization
    private String extractUserIdFromToken(HttpServletRequest request) {
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                return null;
            }
            String token = header.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", String.class);
        } catch (Exception e) {
            // Có thể log lỗi nếu cần
            return null;
        }
    }

    // 1. User/Admin tạo report, ghi đè userId từ token
    @PostMapping
    public ResponseEntity<?> createReport(@Valid @RequestBody ReportRequest request,
                                          HttpServletRequest httpRequest) {
        String userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }
        request.setUserId(userId);
        ReportResponse response = reportTourService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. ADMIN xem tất cả report (phân trang)
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ReportResponse> reports = reportTourService.getAllReports(page, size);
        return ResponseEntity.ok(reports);
    }

    // 3. ADMIN duyệt (RESOLVE) report - giữ nguyên param adminUserId
    @PutMapping("/{reportId}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable Long reportId,
            @RequestParam String adminUserId) {
        ReportResponse response = reportTourService.resolveReport(reportId, adminUserId);
        return ResponseEntity.ok(response);
    }

    // 4. ADMIN xóa report - giữ nguyên param adminUserId
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport(
            @PathVariable Long reportId,
            @RequestParam String adminUserId) {
        reportTourService.deleteReport(reportId, adminUserId);
        return ResponseEntity.noContent().build();
    }

    // 5. User/Admin tự hủy report của mình, lấy userId từ token
    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelReport(@RequestParam String tourId,
                                          HttpServletRequest httpRequest) {
        String userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }
        reportTourService.cancelReport(tourId, userId);
        return ResponseEntity.noContent().build();
    }

    // 6. Lấy lịch sử report của user, userId lấy từ token
    @GetMapping("/history")
    public ResponseEntity<?> getUserReportHistory(HttpServletRequest httpRequest) {
        String userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }
        List<ReportResponse> reports = reportTourService.getReportsByUser(userId);
        return ResponseEntity.ok(reports);
    }

    // 7. Check đã report chưa, lấy userId từ token, không dùng query param userId
    @GetMapping("/check")
    public ResponseEntity<?> hasReported(@RequestParam String tourId,
                                         HttpServletRequest httpRequest) {
        String userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }
        boolean reported = reportTourService.hasReported(tourId, userId);
        return ResponseEntity.ok(reported);
    }
}
