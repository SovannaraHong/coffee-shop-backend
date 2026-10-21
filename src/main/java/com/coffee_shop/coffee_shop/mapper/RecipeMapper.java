package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.RecipeIngredientResponse;
import com.coffee_shop.coffee_shop.dto.response.RecipeResponse;
import com.coffee_shop.coffee_shop.entity.Recipe;
import com.coffee_shop.coffee_shop.entity.RecipeIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.name", target = "productVariantName")
    @Mapping(source = "productVariant.product.id", target = "productId")
    @Mapping(source = "productVariant.product.name", target = "productName")
    @Mapping(source = "recipeIngredients", target = "ingredients")
    RecipeResponse toResponse(Recipe recipe);

    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    @Mapping(source = "ingredient.unit", target = "ingredientUnit")
    RecipeIngredientResponse toIngredientResponse(RecipeIngredient entity);

    List<RecipeIngredientResponse> toIngredientResponseList(List<RecipeIngredient> entities);
}
