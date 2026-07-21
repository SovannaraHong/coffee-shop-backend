package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long>, JpaSpecificationExecutor<Variant> {
    boolean existsByName(String name);

    Optional<Variant> findByNameIgnoreCase(String name);

    boolean existsBySku(String sku);

    List<Variant> findByProductId(Long id);

    Optional<Variant> findByProductIdAndNameIgnoreCase(Long productId, String name);
}
