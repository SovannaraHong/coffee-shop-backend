package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Long> {
    Optional<Addon> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}