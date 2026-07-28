package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
}
