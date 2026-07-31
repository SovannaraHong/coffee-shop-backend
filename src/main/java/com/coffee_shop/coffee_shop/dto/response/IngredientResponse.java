package com.coffee_shop.coffee_shop.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id",
        "name",
        "unit",
        "quantityInStock",
        "reorderLevel",
        "isLowStock",
        "isActive",
        "createdAt"
})
@Data
public class IngredientResponse {
    private Long id;
    private String name;
    private String unit;
    private BigDecimal quantityInStock;
    private BigDecimal reorderLevel;
    private Boolean isLowStock;
    private Boolean isActive;
    private LocalDateTime createdAt;
}