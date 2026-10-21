package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.RecipeCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.RecipeUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.RecipeResponse;
import com.coffee_shop.coffee_shop.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @Operation(
            summary = "Create recipe for product variant",
            description = """
                    Create a recipe specifying the required ingredients and quantities
                    for a specific product variant (e.g. Latte Large = 18g Beans + 200ml Milk).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recipe created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or recipe already exists for this variant"),
            @ApiResponse(responseCode = "404", description = "Variant or ingredient not found")
    })
    @PostMapping
    public ResponseEntity<RecipeResponse> create(@Valid @RequestBody RecipeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.create(request));
    }

    @Operation(
            summary = "Update recipe",
            description = "Update instructions or ingredient requirements for an existing recipe."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe updated successfully"),
            @ApiResponse(responseCode = "404", description = "Recipe or ingredient not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RecipeUpdateRequest request) {
        return ResponseEntity.ok(recipeService.update(id, request));
    }

    @Operation(summary = "Get recipe by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getById(id));
    }

    @Operation(summary = "Get recipe by Product Variant ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Recipe not found for this variant")
    })
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<RecipeResponse> getByVariantId(@PathVariable Long variantId) {
        return ResponseEntity.ok(recipeService.getByVariantId(variantId));
    }

    @Operation(summary = "Get all recipes")
    @GetMapping
    public ResponseEntity<List<RecipeResponse>> getAll() {
        return ResponseEntity.ok(recipeService.getAll());
    }

    @Operation(summary = "Delete recipe by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Recipe deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
