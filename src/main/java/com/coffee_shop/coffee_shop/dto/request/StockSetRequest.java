package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockSetRequest {

    @NotNull(message = "New quantity is required")
    @DecimalMin(value = "0.0", message = "New quantity cannot be negative")
    private BigDecimal newQuantity;

    @NotBlank(message = "Notes are required for manual stock correction")
    @Size(max = 255)
    private String notes;
}