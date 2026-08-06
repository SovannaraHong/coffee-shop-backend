package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.AddonIngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonIngredientResponse;
import com.coffee_shop.coffee_shop.service.AddonIngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/addons/{addonId}/ingredients")
@RestController
@RequiredArgsConstructor
public class AddonIngredientController {

    private final AddonIngredientService addonIngredientService;


    @Operation(
            summary = "Create addon ingredient",
            description = """
                    Create a new ingredient requirement for an addon.
                    
                    Example:
                    Add Cream (30ml) as an ingredient required by
                    Whipped Cream addon.
                    
                    This relationship is used for inventory calculation
                    when customers order addons.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Addon ingredient created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Addon or ingredient not found")
    })
    @PostMapping
    public ResponseEntity<AddonIngredientResponse> create(
            @PathVariable Long addonId,
            @Valid @RequestBody AddonIngredientCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addonIngredientService.create(addonId, request));
    }


    @Operation(
            summary = "Update addon ingredient",
            description = """
                    Update an existing ingredient requirement of an addon.
                    
                    Example:
                    Change Cream quantity from 30ml to 25ml.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addon ingredient updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Addon ingredient not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AddonIngredientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddonIngredientCreateRequest request) {

        return ResponseEntity.ok(addonIngredientService.update(id, request));
    }


    @Operation(
            summary = "Delete addon ingredient",
            description = """
                    Remove an ingredient requirement from an addon.
                    
                    Example:
                    Remove Sugar from Whipped Cream recipe.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Addon ingredient deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Addon ingredient not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        addonIngredientService.delete(id);

        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Get addon ingredients",
            description = """
                    Get all ingredients required by a specific addon.
                    
                    Example:
                    Get all ingredients used by Whipped Cream addon.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredients retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Addon not found")
    })
    @GetMapping
    public ResponseEntity<List<AddonIngredientResponse>> findByAddonId(
            @PathVariable Long addonId) {

        return ResponseEntity.ok(addonIngredientService.findByAddonId(addonId));
    }
}