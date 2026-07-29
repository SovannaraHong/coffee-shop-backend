package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.IngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.IngredientUpdateRequest;
import com.coffee_shop.coffee_shop.dto.request.StockAdjustRequest;
import com.coffee_shop.coffee_shop.dto.request.StockSetRequest;
import com.coffee_shop.coffee_shop.dto.response.IngredientResponse;
import com.coffee_shop.coffee_shop.entity.Ingredient;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IngredientService {
    IngredientResponse createIngredient(IngredientCreateRequest request);

    IngredientResponse updateIngredient(Long id, IngredientUpdateRequest request);

    void deleteIngredient(Long id);

    Ingredient getIngredientById(Long id);

    List<IngredientResponse> getAllIngredients();

    Page<IngredientResponse> getPagination(Map<String, String> params);


    IngredientResponse increaseStock(Long id, StockAdjustRequest request);

    IngredientResponse decreaseStock(Long id, StockAdjustRequest request);

    //change stock like wrong import
    IngredientResponse adjustStock(Long id, StockSetRequest request);

//    IngredientResponse importStock(Long id, StockImportRequest request);

    List<IngredientResponse> checkLowStock();

    IngredientResponse enableIngredient(Long id);

    IngredientResponse disableIngredient(Long id);
}
