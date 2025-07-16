package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.SubCompanyDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISubCompanyDetailsRepository extends JpaRepository<SubCompanyDetails, String> {
    // Không cần viết gì thêm nếu chỉ dùng findById
}
