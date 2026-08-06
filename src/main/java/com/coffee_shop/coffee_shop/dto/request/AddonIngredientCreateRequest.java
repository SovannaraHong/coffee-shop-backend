package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddonIngredientCreateRequest {
    @NotNull(message = "Ingredient id is required")
    private Long ingredientId;

    @NotNull(message = "Quantity required is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantityRequired;
}