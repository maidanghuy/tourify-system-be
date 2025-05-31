package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserName(String userName);
}
