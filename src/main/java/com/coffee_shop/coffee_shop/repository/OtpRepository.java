package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
}