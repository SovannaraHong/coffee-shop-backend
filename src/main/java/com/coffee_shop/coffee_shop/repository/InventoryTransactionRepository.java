package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.InventoryTransaction;
import com.coffee_shop.coffee_shop.util.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long>, JpaSpecificationExecutor<InventoryTransaction> {

    List<InventoryTransaction> findByIngredientId(Long ingredientId);


    List<InventoryTransaction> findByTransactionType(TransactionType type);

    List<InventoryTransaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);
}
