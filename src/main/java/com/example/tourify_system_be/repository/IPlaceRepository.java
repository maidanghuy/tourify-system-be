package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPlaceRepository extends JpaRepository<Place,String> {
}
