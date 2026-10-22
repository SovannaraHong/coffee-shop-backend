package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.mapper.ProductMapper;
import com.coffee_shop.coffee_shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    // ==================================================
    // 🌐 PUBLIC — no token needed (handled in SecurityConfig)
    // ==================================================

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping("/{id}/category")
    public ResponseEntity<List<ProductResponse>> findProductByCategoryId(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findProductByCategoryId(id));
    }

    @GetMapping("/feature")
    public ResponseEntity<List<ProductResponse>> findFeatureProducts() {
        return ResponseEntity.ok(productService.findFeaturedProducts());
    }

    @GetMapping("/new")
    public ResponseEntity<List<ProductResponse>> findNewestProducts() {
        return ResponseEntity.ok(productService.findNewestProducts());
    }

    // ==================================================
    // 🔵 READ — staff paginated list
    // ==================================================

    @PreAuthorize("hasAnyAuthority('PRODUCT_READ', 'PRODUCT_MANAGE')")
    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<ProductResponse>> getProducts(
            @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(productService.getPagination(params));
    }

    // ==================================================
    // 🟢 CREATE
    // ==================================================

    @PreAuthorize("hasAnyAuthority('PRODUCT_CREATE', 'PRODUCT_MANAGE')")
    @PostMapping("/create")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(productRequest));
    }

    // ==================================================
    // 🟠 UPDATE — upload image
    // ==================================================

    @PreAuthorize("hasAnyAuthority('PRODUCT_UPDATE', 'PRODUCT_MANAGE')")
    @PutMapping("/{id}/image")
    public ResponseEntity<ProductResponse> uploadProductImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(productService.uploadProductImage(id, file));
    }

    // ==================================================
    // 🟠 UPDATE — edit fields
    // ==================================================

    @PreAuthorize("hasAnyAuthority('PRODUCT_UPDATE', 'PRODUCT_MANAGE')")
    @PutMapping("/{id}/update")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    // ==================================================
    // 🟠 UPDATE — toggle active / inactive status
    // ==================================================

    @PreAuthorize("hasAnyAuthority('PRODUCT_UPDATE', 'PRODUCT_MANAGE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponse> changeStatus(@PathVariable Long id) {
        return ResponseEntity.ok(productService.changeProductStatus(id));
    }

    // ==================================================
    // 🟠 UPDATE — toggle featured flag
    // ==================================================

    @PreAuthorize("hasAnyAuthority('PRODUCT_UPDATE', 'PRODUCT_MANAGE')")
    @PatchMapping("/{id}/feature")
    public ResponseEntity<ProductResponse> changeFeature(@PathVariable Long id) {
        return ResponseEntity.ok(productService.changeFeatureStatus(id));
    }

    // ==================================================
    // 🔴 DELETE
    // ==================================================

    @PreAuthorize("hasAnyAuthority('PRODUCT_DELETE', 'PRODUCT_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
