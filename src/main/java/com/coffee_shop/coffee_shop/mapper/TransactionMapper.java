package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.TransactionResponse;
import com.coffee_shop.coffee_shop.entity.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "type", source = "transactionType")
    @Mapping(target = "note", source = "notes")
    TransactionResponse toResponse(InventoryTransaction transaction);
}