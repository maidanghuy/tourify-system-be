package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.FollowSubCompany;
import com.example.tourify_system_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFollowSubCompanyRepository extends JpaRepository<FollowSubCompany, String> {

    // Kiểm tra customer đã follow sub-company này chưa
    boolean existsByCustomerAndSubCompany(User customer, User subCompany);

    // Xóa follow
    void deleteByCustomerAndSubCompany(User customer, User subCompany);

    // Lấy follow nếu tồn tại (optional)
    Optional<FollowSubCompany> findByCustomerAndSubCompany(User customer, User subCompany);

    // Đếm số follower của 1 sub-company
    long countBySubCompany(User subCompany);

    // Đếm số sub-company 1 customer đã follow
    long countByCustomer(User customer);

    // (Optional) Liệt kê tất cả các sub-company user đã follow
    // List<FollowSubCompany> findAllByCustomer(User customer);

    // (Optional) Liệt kê tất cả customer đã follow 1 sub-company
    // List<FollowSubCompany> findAllBySubCompany(User subCompany);


    // (Optional) Liệt kê tất cả customer đã follow 1 sub-company
    List<FollowSubCompany> findAllBySubCompany(User subCompany);

// Đếm số follower của 1 sub-company theo userId (P thêm)
    int countBySubCompany_UserId(String userId);
}
