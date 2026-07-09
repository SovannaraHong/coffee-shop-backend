package com.coffee_shop.coffee_shop.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;

@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "price",
        "discount",
        "salePrice",
        "stock",
        "image",
        "size",
        "status",
        "categoryName"
})
@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal salePrice;
    private Integer stock;
    private String image;
    private String size;
    private Boolean status;
    private String categoryName;

}
