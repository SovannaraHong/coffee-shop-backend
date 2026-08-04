package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.AddonCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonResponse;
import com.coffee_shop.coffee_shop.service.AddonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/addons")
@RestController
@RequiredArgsConstructor
public class AddonController {

    private final AddonService addonService;

    @PostMapping
    public ResponseEntity<AddonResponse> create(@Valid @RequestBody AddonCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addonService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddonResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody AddonCreateRequest request) {
        return ResponseEntity.ok(addonService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddonResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(addonService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<AddonResponse>> getAll() {
        return ResponseEntity.ok(addonService.getAll());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AddonResponse> changeStatus(@PathVariable Long id) {
        return ResponseEntity.ok(addonService.changeStatus(id));
    }
}