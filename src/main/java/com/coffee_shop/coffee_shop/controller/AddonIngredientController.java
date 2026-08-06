package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.AddonIngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonIngredientResponse;
import com.coffee_shop.coffee_shop.service.AddonIngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//This API is used when you want to manage the ingredients that make up an addon.
//When an admin creates or edits an addon, they need to specify which ingredients it uses.
@RequestMapping("api/addons/{addonId}/ingredients")
@RestController
@RequiredArgsConstructor
public class AddonIngredientController {

    private final AddonIngredientService addonIngredientService;

    @PostMapping
    public ResponseEntity<AddonIngredientResponse> create(@PathVariable Long addonId,
                                                          @Valid @RequestBody AddonIngredientCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addonIngredientService.create(addonId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddonIngredientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddonIngredientCreateRequest request) {
        return ResponseEntity.ok(addonIngredientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addonIngredientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AddonIngredientResponse>> findByAddonId(@PathVariable Long addonId) {
        return ResponseEntity.ok(addonIngredientService.findByAddonId(addonId));
    }
}