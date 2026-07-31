package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.StockAdjustmentResponse;
import com.coffee_shop.coffee_shop.entity.StockAdjustment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockAdjustmentMapper {
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    StockAdjustmentResponse toResponse(StockAdjustment stockAdjustment);
}
