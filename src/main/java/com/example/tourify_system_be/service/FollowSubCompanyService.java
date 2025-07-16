package com.example.tourify_system_be.service;

import com.example.tourify_system_be.entity.FollowSubCompany;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.repository.IFollowSubCompanyRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowSubCompanyService {

    private final IFollowSubCompanyRepository followRepo;
    private final IUserRepository userRepo;

    /**
     * Customer follow một Sub-company.
     */
    @Transactional
    public void follow(String customerId, String subCompanyId) {
        if (customerId.equals(subCompanyId)) {
            throw new IllegalArgumentException("Không thể follow chính mình.");
        }

        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer không tồn tại"));
        User subCompany = userRepo.findById(subCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-company không tồn tại"));

        if (!"user".equalsIgnoreCase(customer.getRole())) {
            throw new IllegalArgumentException("Chỉ customer (role 'user') mới được follow.");
        }
        if (!"SUB_COMPANY".equalsIgnoreCase(subCompany.getRole())) {
            throw new IllegalArgumentException("Chỉ được follow user có role 'SUB_COMPANY'.");
        }
        if (followRepo.existsByCustomerAndSubCompany(customer, subCompany)) {
            throw new IllegalArgumentException("Đã follow sub-company này.");
        }

        FollowSubCompany follow = FollowSubCompany.builder()
                // Không set followId, Hibernate sẽ tự sinh!
                .customer(customer)
                .subCompany(subCompany)
                .build();
        followRepo.save(follow);
    }

    /**
     * Customer unfollow một Sub-company.
     */
    @Transactional
    public void unfollow(String customerId, String subCompanyId) {
        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer không tồn tại"));
        User subCompany = userRepo.findById(subCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-company không tồn tại"));

        if (!"user".equalsIgnoreCase(customer.getRole())) {
            throw new IllegalArgumentException("Chỉ customer (role 'user') mới được unfollow.");
        }
        if (!"SUB_COMPANY".equalsIgnoreCase(subCompany.getRole())) {
            throw new IllegalArgumentException("Chỉ được unfollow user có role 'SUB_COMPANY'.");
        }

        if (!followRepo.existsByCustomerAndSubCompany(customer, subCompany)) {
            throw new IllegalArgumentException("Chưa follow nên không thể unfollow.");
        }
        followRepo.deleteByCustomerAndSubCompany(customer, subCompany);
    }

    /**
     * Kiểm tra đã follow chưa (để hiển thị trạng thái nút).
     */
    public boolean isFollowed(String customerId, String subCompanyId) {
        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer không tồn tại"));
        User subCompany = userRepo.findById(subCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-company không tồn tại"));
        return followRepo.existsByCustomerAndSubCompany(customer, subCompany);
    }

    /**
     * Đếm số follower của một sub-company.
     */
    public long countFollower(String subCompanyId) {
        User subCompany = userRepo.findById(subCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-company không tồn tại"));
        return followRepo.countBySubCompany(subCompany);
    }

    /**
     * Đếm số sub-company mà một customer đã follow.
     */
    public long countFollowed(String customerId) {
        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer không tồn tại"));
        return followRepo.countByCustomer(customer);
    }
}
