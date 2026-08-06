package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.AddonIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddonIngredientRepository extends JpaRepository<AddonIngredient, Long> {
    List<AddonIngredient> findByAddonId(Long addonId);
}