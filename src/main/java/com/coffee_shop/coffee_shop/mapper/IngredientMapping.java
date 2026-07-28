package com.coffee_shop.coffee_shop.mapper;


import com.coffee_shop.coffee_shop.dto.request.IngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.IngredientUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.IngredientResponse;
import com.coffee_shop.coffee_shop.entity.Ingredient;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IngredientMapping {
    Ingredient toEntity(IngredientCreateRequest request);

    IngredientResponse toResponse(Ingredient ingredient);

    void updateEntity(@MappingTarget Ingredient ingredient, IngredientUpdateRequest request);

    @AfterMapping
    default void setIsLowStock(Ingredient ingredient, @MappingTarget IngredientResponse response) {
        response.setIsLowStock(
                ingredient.getQuantityInStock().compareTo(ingredient.getReorderLevel()) <= 0
        );
    }
}
