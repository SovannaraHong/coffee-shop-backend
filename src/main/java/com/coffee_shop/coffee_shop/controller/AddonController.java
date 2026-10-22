package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.AddonCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonResponse;
import com.coffee_shop.coffee_shop.service.AddonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addons")
@RequiredArgsConstructor
public class AddonController {

    private final AddonService addonService;

    // ── READ — public list (no token needed, handled in SecurityConfig) ───────
    @GetMapping
    public ResponseEntity<List<AddonResponse>> getAll() {
        return ResponseEntity.ok(addonService.getAll());
    }

    // ── READ — public single ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<AddonResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(addonService.findById(id));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ADDON_CREATE', 'ADDON_MANAGE')")
    @PostMapping
    public ResponseEntity<AddonResponse> create(@Valid @RequestBody AddonCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addonService.create(request));
    }

    // ── UPDATE — edit fields ──────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ADDON_UPDATE', 'ADDON_MANAGE')")
    @PutMapping("/{id}")
    public ResponseEntity<AddonResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddonCreateRequest request) {
        return ResponseEntity.ok(addonService.update(id, request));
    }

    // ── UPDATE — toggle active/inactive status ────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ADDON_UPDATE', 'ADDON_MANAGE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AddonResponse> changeStatus(@PathVariable Long id) {
        return ResponseEntity.ok(addonService.changeStatus(id));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ADDON_DELETE', 'ADDON_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}