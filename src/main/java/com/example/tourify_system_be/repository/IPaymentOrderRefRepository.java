package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.PaymentOrderRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPaymentOrderRefRepository extends JpaRepository<PaymentOrderRef, Long> {
}
