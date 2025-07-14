package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    Optional<PaymentMethod> findByMethodName(String name); // code: "VIETQR", "CASH", ...

}
