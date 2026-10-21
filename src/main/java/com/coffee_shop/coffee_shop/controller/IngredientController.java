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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/ingredient")
public class IngredientController {
    private final IngredientService ingredientService;
    private final IngredientMapping ingredientMapping;

    @PostMapping("/create")
    public ResponseEntity<IngredientResponse> create(@Valid @RequestBody IngredientCreateRequest request) {
        IngredientResponse ingredient = ingredientService.createIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredient);
    }

    @PostMapping("/{id}/increment-stock")
    public ResponseEntity<IngredientResponse> incrementStock(@PathVariable Long id,
                                                             @Valid @RequestBody StockAdjustRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ingredientService.increaseStock(id, request));
    }

    @PostMapping("/{id}/decrement-stock")
    public ResponseEntity<IngredientResponse> decrementStock(@PathVariable Long id,
                                                             @Valid @RequestBody StockAdjustRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ingredientService.decreaseStock(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok().body(ingredientMapping.toResponse(ingredientService.getIngredientById(id)));
    }

    // CHANGED: added @Valid. IngredientUpdateRequest has @NotBlank/@Size rules that were never enforced.
    @PutMapping("/{id}/update")
    public ResponseEntity<IngredientResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody IngredientUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ingredientService.updateIngredient(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAll() {
        return ResponseEntity.ok().body(ingredientService.getAllIngredients());
    }

    // CHANGED: @PathVariable -> @RequestParam. This route has no path variables, so the old
    // version could never receive ?page=1&size=10 style parameters.
    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<IngredientResponse>> getPagination(@RequestParam Map<String, String> params) {
        Page<IngredientResponse> pagination = ingredientService.getPagination(params);
        return ResponseEntity.ok().body(new PageDTO<>(pagination));
    }

    // CHANGED: added @Valid so StockSetRequest's "notes are required" rule is enforced.
    @PostMapping("/{id}/adjust-stock")
    public ResponseEntity<IngredientResponse> adjustStock(@PathVariable Long id,
                                                          @Valid @RequestBody StockSetRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ingredientService.adjustStock(id, request));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<IngredientResponse>> getLowStock() {
        return ResponseEntity.ok().body(ingredientService.checkLowStock());
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<IngredientResponse> enableIngredient(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ingredientService.enableIngredient(id));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<IngredientResponse> disableIngredient(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ingredientService.disableIngredient(id));
    }
}