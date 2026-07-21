package com.coffee_shop.coffee_shop.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "imageUrl",
        "isActive",
        "categoryName"
})
@Data
public class ProductResponse {

    private Long id;

    private String name;

    private String description;

    private String imageUrl;

    private Boolean isActive;
    private Boolean featured;
    private Long categoryId;
    private String categoryName;
    private List<VariantResponse> variants;

}