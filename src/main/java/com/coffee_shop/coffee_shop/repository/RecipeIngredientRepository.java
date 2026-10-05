package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    List<RecipeIngredient> findByRecipeId(Long recipeId);
    void deleteByRecipeId(Long recipeId);
}
