package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_adjustments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "current_stock")
    private BigDecimal currentStock;

    @Column(nullable = false, length = 255)
    private String notes;


}