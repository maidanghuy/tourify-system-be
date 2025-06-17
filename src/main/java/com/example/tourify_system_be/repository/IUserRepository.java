package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)

public interface IUserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserName(String userName);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByPhoneNumber(String phoneNumber);
}
