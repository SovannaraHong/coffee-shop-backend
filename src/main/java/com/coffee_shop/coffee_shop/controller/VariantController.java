package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.VariantRequest;
import com.coffee_shop.coffee_shop.dto.response.VariantResponse;
import com.coffee_shop.coffee_shop.service.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/variant")
@RequiredArgsConstructor
public class VariantController {
    private final VariantService variantService;

    @PostMapping("/{productId}/variants")
    public ResponseEntity<VariantResponse> create(
            @PathVariable Long productId,
            @Valid @RequestBody VariantRequest request) {
        VariantResponse response = variantService.create(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariantResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok().body(variantService.findById(id));
    }

}
