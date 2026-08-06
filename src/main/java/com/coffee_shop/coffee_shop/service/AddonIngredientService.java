package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.AddonIngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonIngredientResponse;

import java.util.List;

public interface AddonIngredientService {
    AddonIngredientResponse create(Long addonId, AddonIngredientCreateRequest request);

    AddonIngredientResponse update(Long id, AddonIngredientCreateRequest request);

    void delete(Long id);

    List<AddonIngredientResponse> findByAddonId(Long addonId);
}