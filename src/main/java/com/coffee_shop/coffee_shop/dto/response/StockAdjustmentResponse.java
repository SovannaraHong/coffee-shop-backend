package com.coffee_shop.coffee_shop.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id",
        "ingredientId",
        "ingredientName",
        "oldQuantity",
        "newQuantity",
        "currentStock",
        "notes",
        "createdAt"
})
@Data
public class StockAdjustmentResponse {

    private Long id;

    private Long ingredientId;

    private String ingredientName;

    private BigDecimal oldQuantity;

    private BigDecimal newQuantity;

    private BigDecimal currentStock;
    private String notes;

    private LocalDateTime createdAt;
}