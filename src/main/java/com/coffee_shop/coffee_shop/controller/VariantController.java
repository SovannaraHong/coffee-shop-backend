package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.VariantRequest;
import com.coffee_shop.coffee_shop.dto.response.VariantResponse;
import com.coffee_shop.coffee_shop.service.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @GetMapping()
    public ResponseEntity<List<VariantResponse>> getAll() {
        return ResponseEntity.ok().body(variantService.getAll());
    }

    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<VariantResponse>> getPagination(Map<String, String> params) {
        Page<VariantResponse> pagination = variantService.getPagination(params);
        return ResponseEntity.ok().body(new PageDTO<>(pagination));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<VariantResponse>> findByProductId(@PathVariable Long id) {
        return ResponseEntity.ok().body(variantService.findByProductId(id));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        variantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/changeStatus")
    public ResponseEntity<VariantResponse> changeStatus(@PathVariable Long id) {
        return ResponseEntity.ok().body(variantService.changeStatus(id));
    }

    @PatchMapping("/{id}/changePrice")
    public ResponseEntity<VariantResponse> changePrice(
            @PathVariable Long id,
            @RequestBody BigDecimal price
    ) {
        return ResponseEntity.ok().body(
                variantService.updatePrice(id, price)
        );
    }


}
