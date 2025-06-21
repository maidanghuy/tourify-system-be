package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.BookingsTour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingTourRepository extends JpaRepository<BookingsTour, String> {
}