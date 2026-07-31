package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockImportRequest {

    @NotNull(message = "Import quantity is required")
    @DecimalMin(value = "0.01", message = "Import quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Notes are required for stock import")
    @Size(max = 255)
    private String notes;
}