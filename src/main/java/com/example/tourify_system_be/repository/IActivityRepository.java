package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IActivityRepository extends JpaRepository<Activity, String> {}
