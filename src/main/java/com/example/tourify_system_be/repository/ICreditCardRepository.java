package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.CreditCard;
import com.example.tourify_system_be.entity.Feedback;
import com.example.tourify_system_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICreditCardRepository extends JpaRepository<CreditCard, String> {
    List<CreditCard> findAllByUser_UserId(String id);
}
