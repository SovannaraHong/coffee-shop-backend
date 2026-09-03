package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.IpLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpLoginAttemptRepository extends JpaRepository<IpLoginAttempt, String> {
}