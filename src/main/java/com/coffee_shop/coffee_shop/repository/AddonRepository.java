package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Addon;
import com.coffee_shop.coffee_shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Long> {
    Optional<Addon> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("select p.id from Product p join p.addons a where a.id = :addonId")
    List<Long> findProductIdsByAddonId(@Param("addonId") Long addonId);

    @Query("select p from Product p join p.addons a where a.id = :addonId")
    List<Product> findProductsByAddonId(@Param("addonId") Long addonId);

    @Query("select count(p) > 0 from Product p join p.addons a where a.id = :addonId")
    boolean isUsedByProducts(@Param("addonId") Long addonId);
}