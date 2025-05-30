package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, String> {
}
