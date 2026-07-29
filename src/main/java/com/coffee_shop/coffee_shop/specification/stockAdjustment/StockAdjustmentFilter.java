package com.coffee_shop.coffee_shop.specification.stockAdjustment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockAdjustmentFilter {
    private Long ingredientId;
    private LocalDateTime fromDate;   // adjustments created on/after this
    private LocalDateTime toDate;     // adjustments created on/before this
}