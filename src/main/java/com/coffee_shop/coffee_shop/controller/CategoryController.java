package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.CategoryRequest;
import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.mapper.CategoryMapper;
import com.coffee_shop.coffee_shop.service.CategoryService;
import com.coffee_shop.coffee_shop.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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


    @Operation(
            summary = "Get category by ID",
            description = """
                    Retrieve category details by category ID.
                    
                    Example:
                    Get Coffee category information.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        Category cateId = categoryService.findById(id);
        return ResponseEntity.ok().body(categoryMapper.toResponse(cateId));
    }


    @Operation(
            summary = "Create new category",
            description = """
                    Create a new category.
                    
                    Example:
                    Create categories such as Coffee, Tea, Dessert,
                    or other product groups.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data")
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CategoryRequest categoryRequest) {

        Category category = categoryService.createCategory(categoryMapper.toEntity(categoryRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryMapper.toResponse(category));

    }


    @Operation(
            summary = "Update category",
            description = """
                    Update existing category information.
                    
                    Example:
                    Update category name or description.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid CategoryRequest categoryRequest) {

        Category updated = categoryService.update(id, categoryRequest);
        return ResponseEntity.ok().body(categoryMapper.toResponse(updated));
    }


    @Operation(
            summary = "Delete category",
            description = """
                    Delete category by ID.
                    
                    The category will be removed from the system.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponse> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Get categories with pagination",
            description = """
                    Retrieve categories using pagination and filtering.
                    
                    Supported parameters:
                    - page
                    - size
                    - search
                    - sorting
                    
                    Example:
                    /api/categories?page=0&size=10
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<?> getCategories(
            @RequestParam Map<String, String> params) {

        Page<CategoryResponse> categoriesPageable =
                categoryService.getCategoriesPageable(params);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new PageDTO<>(categoriesPageable));
    }


    @Operation(
            summary = "Upload category image",
            description = """
                    Upload category image to AWS S3.
                    
                    If the category already has an image,
                    the old image will be removed first.
                    
                    Example:
                    Upload Coffee category image.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PatchMapping("/{id}/image")
    public ResponseEntity<CategoryResponse> uploadImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile file) throws IOException {

        Category categoryId = categoryService.findById(id);

        if (categoryId.getImageUrl() != null &&
                categoryId.getImageUrl().startsWith("https://")) {

            s3Service.deleteFile(categoryId.getImageUrl());
        }

        String url = s3Service.uploadFile(file, "category_images");

        CategoryResponse categoryResponse = categoryService.updateImage(id, url);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(categoryResponse);

    }


    @Operation(
            summary = "Change category status",
            description = """
                    Change category active status.
                    
                    Example:
                    Activate or deactivate a category
                    without deleting it.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Category status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PatchMapping("/{id}/stutusChange")
    public ResponseEntity<CategoryResponse> changeStatus(
            @PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(categoryService.changeCategoryStatus(id));
    }


    @Operation(
            summary = "Get active categories",
            description = """
                    Retrieve only active categories.
                    
                    Used by customer applications
                    to display available categories.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active categories retrieved successfully")
    })
    @GetMapping("/activeCategory")
    public ResponseEntity<List<CategoryResponse>> findActiveCategory() {
        return ResponseEntity.ok().body(
                categoryService.findActiveCategories()
        );

    }

}