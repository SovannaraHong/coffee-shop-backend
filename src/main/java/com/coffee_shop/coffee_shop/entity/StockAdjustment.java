package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_adjustments")
@Data
@Builder
public class StockAdjustment extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false)
    private BigDecimal oldQuantity;

    @Column(nullable = false)
    private BigDecimal newQuantity;

    @Column(nullable = false, length = 255)
    private String notes;


}