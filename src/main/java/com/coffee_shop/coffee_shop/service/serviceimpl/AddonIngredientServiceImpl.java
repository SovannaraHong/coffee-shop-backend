package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.AddonIngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonIngredientResponse;
import com.coffee_shop.coffee_shop.entity.Addon;
import com.coffee_shop.coffee_shop.entity.AddonIngredient;
import com.coffee_shop.coffee_shop.entity.Ingredient;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.AddonIngredientMapper;
import com.coffee_shop.coffee_shop.repository.AddonIngredientRepository;
import com.coffee_shop.coffee_shop.repository.AddonRepository;
import com.coffee_shop.coffee_shop.repository.IngredientRepository;
import com.coffee_shop.coffee_shop.service.AddonIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddonIngredientServiceImpl implements AddonIngredientService {

    private final AddonIngredientRepository addonIngredientRepository;
    private final AddonRepository addonRepository;
    private final IngredientRepository ingredientRepository;
    private final AddonIngredientMapper addonIngredientMapper;

    @Override
    @Transactional
    public AddonIngredientResponse create(Long addonId, AddonIngredientCreateRequest request) {
        Addon addon = addonRepository.findById(addonId)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Addon", addonId));

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Ingredient", request.getIngredientId()));

        AddonIngredient addonIngredient = AddonIngredient.builder()
                .addon(addon)
                .ingredient(ingredient)
                .quantityRequired(request.getQuantityRequired())
                .build();

        return addonIngredientMapper.toResponse(addonIngredientRepository.save(addonIngredient));
    }

    @Override
    @Transactional
    public AddonIngredientResponse update(Long id, AddonIngredientCreateRequest request) {
        AddonIngredient addonIngredient = findRequired(id);

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Ingredient", request.getIngredientId()));

        addonIngredient.setIngredient(ingredient);
        addonIngredient.setQuantityRequired(request.getQuantityRequired());

        return addonIngredientMapper.toResponse(addonIngredientRepository.save(addonIngredient));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        addonIngredientRepository.delete(findRequired(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddonIngredientResponse> findByAddonId(Long addonId) {
        return addonIngredientRepository.findByAddonId(addonId).stream()
                .map(addonIngredientMapper::toResponse)
                .toList();
    }

    private AddonIngredient findRequired(Long id) {
        return addonIngredientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("AddonIngredient", id));
    }
}