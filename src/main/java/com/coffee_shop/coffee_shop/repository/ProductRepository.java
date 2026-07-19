package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findTop10ByOrderByCreatedAtDesc();
}
