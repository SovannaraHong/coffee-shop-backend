package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderCreateRequest {

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotEmpty(message = "Purchase order must have at least one line item")
    @Valid
    private List<PurchaseOrderDetailRequest> details;

    @Data
    public static class PurchaseOrderDetailRequest {
        @NotNull(message = "Ingredient is required")
        private Long ingredientId;

        @NotNull(message = "Quantity is required")
        private BigDecimal quantity;

        @NotNull(message = "Unit cost is required")
        private BigDecimal unitCost;
    }
}