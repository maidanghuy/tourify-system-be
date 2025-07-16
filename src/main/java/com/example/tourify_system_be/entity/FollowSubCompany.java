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
@Table(name = "follow_subcompanies", schema = "Tourify")
public class FollowSubCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "follow_id", nullable = false, updatable = false, length = 36)
    String followId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_follow_customer"))
    User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_follow_subcompany"))
    User subCompany;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
