package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {

    @NotEmpty(message = "Product name is required.")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Category is required.")
    private Long categoryId;

    private Boolean isActive;

}