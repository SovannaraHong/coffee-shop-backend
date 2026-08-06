package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.AddonIngredientResponse;
import com.coffee_shop.coffee_shop.entity.AddonIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddonIngredientMapper {

    @Mapping(source = "addon.id", target = "addonId")
    @Mapping(source = "addon.name", target = "addonName")
    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    @Mapping(source = "ingredient.unit", target = "ingredientUnit")
    AddonIngredientResponse toResponse(AddonIngredient entity);
}