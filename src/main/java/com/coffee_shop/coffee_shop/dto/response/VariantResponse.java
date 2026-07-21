package com.coffee_shop.coffee_shop.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;

@JsonPropertyOrder({
        "id",
        "productName",
        "name",
        "price",
        "sku",
        "isActive"
})
@Data
public class VariantResponse {

    private Long id;

    private String productName;

    private String name;

    private BigDecimal price;

    private String sku;

    private Boolean isActive;

}