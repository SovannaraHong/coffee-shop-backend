package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.RecipeCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.RecipeUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.RecipeResponse;

import java.util.List;

public interface RecipeService {
    RecipeResponse create(RecipeCreateRequest request);
    RecipeResponse update(Long id, RecipeUpdateRequest request);
    RecipeResponse getById(Long id);
    RecipeResponse getByVariantId(Long variantId);
    List<RecipeResponse> getAll();
    void delete(Long id);
}
