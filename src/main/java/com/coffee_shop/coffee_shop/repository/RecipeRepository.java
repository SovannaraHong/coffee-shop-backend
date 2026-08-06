package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByProductVariantId(Long variantId);
}