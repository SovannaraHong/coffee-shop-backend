package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long>, JpaSpecificationExecutor<Ingredient> {
    Optional<Ingredient> findByNameIgnoreCase(String name);

    @Query("SELECT I FROM Ingredient I WHERE I.quantityInStock<=I.reorderLevel")
    List<Ingredient> findLowStockIngredient();
}
