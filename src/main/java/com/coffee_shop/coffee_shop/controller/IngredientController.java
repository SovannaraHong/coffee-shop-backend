package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.IngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.IngredientUpdateRequest;
import com.coffee_shop.coffee_shop.dto.request.StockAdjustRequest;
import com.coffee_shop.coffee_shop.dto.request.StockSetRequest;
import com.coffee_shop.coffee_shop.dto.response.IngredientResponse;
import com.coffee_shop.coffee_shop.mapper.IngredientMapping;
import com.coffee_shop.coffee_shop.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ingredient")
public class IngredientController {

    private final IngredientService ingredientService;
    private final IngredientMapping ingredientMapping;

    // ── READ — single ─────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_READ', 'INGREDIENT_MANAGE')")
    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ingredientMapping.toResponse(ingredientService.getIngredientById(id))
        );
    }

    // ── READ — all ────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_READ', 'INGREDIENT_MANAGE')")
    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAll() {
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }

    // ── READ — pagination ─────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_READ', 'INGREDIENT_MANAGE')")
    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<IngredientResponse>> getPagination(
            @RequestParam Map<String, String> params) {
        Page<IngredientResponse> pagination = ingredientService.getPagination(params);
        return ResponseEntity.ok(new PageDTO<>(pagination));
    }

    // ── READ — low-stock alert list ───────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_READ', 'INGREDIENT_MANAGE')")
    @GetMapping("/low-stock")
    public ResponseEntity<List<IngredientResponse>> getLowStock() {
        return ResponseEntity.ok(ingredientService.checkLowStock());
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_CREATE', 'INGREDIENT_MANAGE')")
    @PostMapping("/create")
    public ResponseEntity<IngredientResponse> create(@Valid @RequestBody IngredientCreateRequest request) {
        IngredientResponse ingredient = ingredientService.createIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredient);
    }

    // ── UPDATE — edit fields ──────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_UPDATE', 'INGREDIENT_MANAGE')")
    @PutMapping("/{id}/update")
    public ResponseEntity<IngredientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody IngredientUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ingredientService.updateIngredient(id, request));
    }

    // ── UPDATE — increment stock ──────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_UPDATE', 'INGREDIENT_MANAGE')")
    @PostMapping("/{id}/increment-stock")
    public ResponseEntity<IngredientResponse> incrementStock(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ingredientService.increaseStock(id, request));
    }

    // ── UPDATE — decrement stock ──────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_UPDATE', 'INGREDIENT_MANAGE')")
    @PostMapping("/{id}/decrement-stock")
    public ResponseEntity<IngredientResponse> decrementStock(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ingredientService.decreaseStock(id, request));
    }

    // ── UPDATE — set stock (manual override) ─────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_UPDATE', 'INGREDIENT_MANAGE')")
    @PostMapping("/{id}/adjust-stock")
    public ResponseEntity<IngredientResponse> adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody StockSetRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ingredientService.adjustStock(id, request));
    }

    // ── UPDATE — enable ingredient ────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_UPDATE', 'INGREDIENT_MANAGE')")
    @PatchMapping("/{id}/enable")
    public ResponseEntity<IngredientResponse> enableIngredient(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ingredientService.enableIngredient(id));
    }

    // ── UPDATE — disable ingredient ───────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_UPDATE', 'INGREDIENT_MANAGE')")
    @PatchMapping("/{id}/disable")
    public ResponseEntity<IngredientResponse> disableIngredient(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ingredientService.disableIngredient(id));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('INGREDIENT_DELETE', 'INGREDIENT_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}
