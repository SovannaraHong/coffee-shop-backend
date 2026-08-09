package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Payment;
import com.coffee_shop.coffee_shop.util.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionRef(String transactionRef);

    List<Payment> findByStatus(PaymentStatus status);
}