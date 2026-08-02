package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Supplier> findByNameIgnoreCase(String name);

    Optional<Supplier> findByEmail(String email);

    Optional<Supplier> findByPhone(String phone);
}
