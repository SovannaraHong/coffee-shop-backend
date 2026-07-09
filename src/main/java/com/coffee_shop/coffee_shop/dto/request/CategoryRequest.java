package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "name must not be blank")
    private String name;

    private String description;

    private String image;
}
