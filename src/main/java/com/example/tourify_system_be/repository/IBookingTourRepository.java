package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.BookingTour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBookingTourRepository extends JpaRepository<BookingTour, String> {
}