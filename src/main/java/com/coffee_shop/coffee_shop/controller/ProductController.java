package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.mapper.ProductMapper;
import com.coffee_shop.coffee_shop.service.ProductService;
import com.coffee_shop.coffee_shop.service.S3Service;
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

@RestController()
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final S3Service s3Service;

    // ---- PUBLIC: customers browsing the menu ----

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok().body(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        Product proId = productService.findById(id);
        ProductResponse response = productMapper.toResponse(proId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}/category")
    public ResponseEntity<List<ProductResponse>> findProductByCategoryId(@PathVariable Long id) {
        return ResponseEntity.ok().body(productService.findProductByCategoryId(id));
    }

    @GetMapping("/feature")
    public ResponseEntity<List<ProductResponse>> findFeatureProducts() {
        return ResponseEntity.ok().body(productService.findFeaturedProducts());
    }

    @GetMapping("/new")
    public ResponseEntity<List<ProductResponse>> findNewestProducts() {
        return ResponseEntity.ok().body(productService.findNewestProducts());
    }

    // ---- STAFF ONLY: management ----

    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<ProductResponse>> getProducts(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok().body(productService.getPagination(params));
    }

    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @PostMapping("/create")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(productRequest));
    }

    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @PutMapping("/{id}/image")
    public ResponseEntity<ProductResponse> uploadProductImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(productService.uploadProductImage(id, file));
    }

    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @PutMapping("/{id}/update")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(productService.update(id, request));
    }

    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponse> changeStatus(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(productService.changeProductStatus(id));
    }


}
