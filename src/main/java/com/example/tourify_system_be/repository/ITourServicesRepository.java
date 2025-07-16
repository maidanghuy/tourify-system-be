package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.TourServices;
import com.example.tourify_system_be.entity.TourServiceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITourServicesRepository extends JpaRepository<TourServices, TourServiceId> {}
