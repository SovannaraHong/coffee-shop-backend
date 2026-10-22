package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.CategoryRequest;
import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.mapper.CategoryMapper;
import com.coffee_shop.coffee_shop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    // ── READ — public list (no token needed, handled in SecurityConfig) ───────
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    // ── READ — public single ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    // ── READ — staff paginated list ──────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('CATEGORY_READ', 'CATEGORY_MANAGE')")
    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<CategoryResponse>> getCategories(
            @RequestParam Map<String, String> params) {
        Page<CategoryResponse> categoriesPageable =
                categoryService.getCategoriesPageable(params);
        return ResponseEntity.ok(new PageDTO<>(categoriesPageable));
    }

    // ── READ — active categories ──────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('CATEGORY_READ', 'CATEGORY_MANAGE')")
    @GetMapping("/activeCategory")
    public ResponseEntity<List<CategoryResponse>> findActiveCategory() {
        return ResponseEntity.ok(categoryService.findActiveCategories());
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('CATEGORY_CREATE', 'CATEGORY_MANAGE')")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest) {
        Category category = categoryService.createCategory(
                categoryMapper.toEntity(categoryRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryMapper.toResponse(category));
    }

    // ── UPDATE — edit fields ──────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('CATEGORY_UPDATE', 'CATEGORY_MANAGE')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest) {
        Category updated = categoryService.update(id, categoryRequest);
        return ResponseEntity.ok(categoryMapper.toResponse(updated));
    }

    // ── UPDATE — upload image ─────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('CATEGORY_UPDATE', 'CATEGORY_MANAGE')")
    @PutMapping("/{id}/image")
    public ResponseEntity<CategoryResponse> uploadProductImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(categoryService.uploadImage(id, file));
    }

    // ── UPDATE — toggle active/inactive status ────────────────────────────────
    @PreAuthorize("hasAnyAuthority('CATEGORY_UPDATE', 'CATEGORY_MANAGE')")
    @PatchMapping("/{id}/stutusChange")
    public ResponseEntity<CategoryResponse> changeStatus(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(categoryService.changeCategoryStatus(id));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('CATEGORY_DELETE', 'CATEGORY_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}