package com.coffee_shop.coffee_shop.dto.response;

import com.coffee_shop.coffee_shop.entity.Product;
import lombok.Data;

import java.util.Set;

@Data
public class CategoryDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Boolean isActive;
    private Set<Product> products;
}
