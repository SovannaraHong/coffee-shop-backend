package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long>, JpaSpecificationExecutor<StockAdjustment> {
    List<StockAdjustment> findByIngredientIdOrderByCreatedAtDesc(Long ingredientId);
}