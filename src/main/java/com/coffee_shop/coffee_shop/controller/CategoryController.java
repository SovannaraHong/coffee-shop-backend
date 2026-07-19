package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.CategoryRequest;
import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.mapper.CategoryMapper;
import com.coffee_shop.coffee_shop.service.CategoryService;
import com.coffee_shop.coffee_shop.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final S3Service s3Service;

//    @GetMapping
//    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
//        List<CategoryResponse> categories = categoryService.getCategories();
//
//        return ResponseEntity.status(HttpStatus.OK).body(categories);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        Category cateId = categoryService.findById(id);
        return ResponseEntity.ok().body(categoryMapper.toResponse(cateId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest categoryRequest) {
        Category category = categoryService.createCategory(categoryMapper.toEntity(categoryRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toResponse(category));

    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                           @RequestBody @Valid CategoryRequest categoryRequest) {

        Category updated = categoryService.update(id, categoryRequest);
        return ResponseEntity.ok().body(categoryMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponse> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getCategories(@RequestParam Map<String, String> params) {
        Page<CategoryResponse> categoriesPageable = categoryService.getCategoriesPageable(params);
        return ResponseEntity.status(HttpStatus.OK).body(new PageDTO<>(categoriesPageable));
    }

    @PatchMapping("/{id}/image")
    public ResponseEntity<CategoryResponse> uploadImage(@PathVariable Long id, @RequestPart("image") MultipartFile file) throws IOException {

        Category categoryId = categoryService.findById(id);
        if (categoryId.getImageUrl() != null && categoryId.getImageUrl().startsWith("https://")) {
            s3Service.deleteFile(categoryId.getImageUrl());
        }
        String url = s3Service.uploadFile(file, "category_images");
        CategoryResponse categoryResponse = categoryService.updateImage(id, url);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(categoryResponse);

    }

    @PatchMapping("/{id}/stutusChange")
    public ResponseEntity<CategoryResponse> changeStatus(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(categoryService.changeCategoryStatus(id));
    }

    @GetMapping("/activeCategory")
    public ResponseEntity<List<CategoryResponse>> findActiveCategory() {
        return ResponseEntity.ok().body(
                categoryService.findActiveCategories()

        );
    }


}