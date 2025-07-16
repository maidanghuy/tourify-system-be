package com.example.tourify_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "report_tour", schema = "Tourify")
public class ReportTour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    Long reportId;

    // FK đến Tour (nhiều report - 1 tour)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    Tour tour;

    // FK đến User (nhiều report - 1 user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "reason_code", nullable = false, length = 32)
    String reasonCode; // INAPPROPRIATE, FRAUD, OTHER

    @Column(name = "description")
    String description;

    @Column(name = "status", nullable = false, length = 16)
    String status; // PENDING, RESOLVED, REJECTED

    @Column(name = "created_at", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    LocalDateTime updatedAt;

    // Bạn có thể bổ sung thêm trường này nếu muốn lưu phản hồi của admin khi xử lý
    // @Column(name = "admin_comment")
    // String adminComment;

    // Hàm tiện ích nghiệp vụ (nếu cần)
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }
}
