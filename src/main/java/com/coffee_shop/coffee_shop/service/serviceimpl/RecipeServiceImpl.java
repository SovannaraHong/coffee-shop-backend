package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.RecipeCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.RecipeIngredientRequest;
import com.coffee_shop.coffee_shop.dto.request.RecipeUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.RecipeResponse;
import com.coffee_shop.coffee_shop.entity.Ingredient;
import com.coffee_shop.coffee_shop.entity.Recipe;
import com.coffee_shop.coffee_shop.entity.RecipeIngredient;
import com.coffee_shop.coffee_shop.entity.Variant;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.RecipeMapper;
import com.coffee_shop.coffee_shop.repository.IngredientRepository;
import com.coffee_shop.coffee_shop.repository.RecipeRepository;
import com.coffee_shop.coffee_shop.repository.VariantRepository;
import com.coffee_shop.coffee_shop.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final VariantRepository variantRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public RecipeResponse create(RecipeCreateRequest request) {
        Variant variant = variantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", request.getProductVariantId()));

        if (recipeRepository.existsByProductVariantId(variant.getId())) {
            throw new BadRequestException("Recipe already exists for variant: " + variant.getName());
        }

        Recipe recipe = Recipe.builder()
                .productVariant(variant)
                .instructions(request.getInstructions())
                .build();

        Set<RecipeIngredient> ingredients = buildRecipeIngredients(recipe, request.getIngredients());
        recipe.setRecipeIngredients(ingredients);

        Recipe saved = recipeRepository.save(recipe);
        return recipeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RecipeResponse update(Long id, RecipeUpdateRequest request) {
        Recipe recipe = getRequiredRecipe(id);

        if (request.getInstructions() != null) {
            recipe.setInstructions(request.getInstructions());
        }

        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            recipe.getRecipeIngredients().clear();
            Set<RecipeIngredient> newIngredients = buildRecipeIngredients(recipe, request.getIngredients());
            recipe.getRecipeIngredients().addAll(newIngredients);
        }

        Recipe saved = recipeRepository.save(recipe);
        return recipeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponse getById(Long id) {
        return recipeMapper.toResponse(getRequiredRecipe(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponse getByVariantId(Long variantId) {
        Recipe recipe = recipeRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found for variant ID: " + variantId));
        return recipeMapper.toResponse(recipe);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeResponse> getAll() {
        return recipeRepository.findAll().stream()
                .map(recipeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Recipe recipe = getRequiredRecipe(id);
        recipeRepository.delete(recipe);
    }

    private Recipe getRequiredRecipe(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Recipe", id));
    }

    private Set<RecipeIngredient> buildRecipeIngredients(Recipe recipe, List<RecipeIngredientRequest> requests) {
        Set<RecipeIngredient> set = new HashSet<>();
        for (RecipeIngredientRequest req : requests) {
            Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> ResourceNotFoundException.notFoundException("Ingredient", req.getIngredientId()));

            RecipeIngredient ri = RecipeIngredient.builder()
                    .recipe(recipe)
                    .ingredient(ingredient)
                    .quantityRequired(req.getQuantityRequired())
                    .build();
            set.add(ri);
        }
        return set;
    }
}
