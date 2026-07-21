package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

// ProductRequest.java
@Data
public class ProductRequest {

    @NotEmpty(message = "Product name is required.")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Category is required.")
    private Long categoryId;

    private Boolean isActive;

    private Boolean featured;

    @NotEmpty(message = "At least one variant is required.")
    @Valid
    private List<VariantRequest> variants;
}