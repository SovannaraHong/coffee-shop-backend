package com.coffee_shop.coffee_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponse {
    private Long id;
    private Long productVariantId;
    private String productVariantName;
    private Long productId;
    private String productName;
    private String instructions;
    private List<RecipeIngredientResponse> ingredients;
}
