package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.AddonIngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonIngredientResponse;
import com.coffee_shop.coffee_shop.service.AddonIngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addons/{addonId}/ingredients")
@RequiredArgsConstructor
public class AddonIngredientController {

    private final AddonIngredientService addonIngredientService;

    // ── READ ─────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ADDON_INGREDIENT_READ', 'ADDON_INGREDIENT_MANAGE')")
    @GetMapping
    public ResponseEntity<List<AddonIngredientResponse>> findByAddonId(
            @PathVariable Long addonId) {
        return ResponseEntity.ok(addonIngredientService.findByAddonId(addonId));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ADDON_INGREDIENT_CREATE', 'ADDON_INGREDIENT_MANAGE')")
    @PostMapping
    public ResponseEntity<AddonIngredientResponse> create(
            @PathVariable Long addonId,
            @Valid @RequestBody AddonIngredientCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addonIngredientService.create(addonId, request));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ADDON_INGREDIENT_UPDATE', 'ADDON_INGREDIENT_MANAGE')")
    @PutMapping("/{id}")
    public ResponseEntity<AddonIngredientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddonIngredientCreateRequest request) {
        return ResponseEntity.ok(addonIngredientService.update(id, request));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ADDON_INGREDIENT_DELETE', 'ADDON_INGREDIENT_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addonIngredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}