package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional(readOnly = true)
public interface IPlaceRepository extends JpaRepository<Place, String> {
}
