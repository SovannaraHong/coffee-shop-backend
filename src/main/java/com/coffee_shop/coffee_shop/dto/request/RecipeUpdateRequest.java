package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeUpdateRequest {

    private String instructions;

    @NotEmpty(message = "Recipe must contain at least one ingredient")
    @Valid
    private List<RecipeIngredientRequest> ingredients;
}
