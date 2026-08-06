package com.coffee_shop.coffee_shop.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({
        "id",
        "addonId",
        "addonName",
        "quantity",
        "unitPrice",
        "subtotal"
})
public class OrderDetailAddonResponse {

    private Long id;
    private Long addonId;
    private String addonName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}