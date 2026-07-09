package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.CategoryRequest;
import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<CategoryResponse> getCategories();

    Category findById(Long id);

    Category createCategory(Category category);

    Category update(Long id, CategoryRequest request);

    void delete(Long id);

    Page<CategoryResponse> getCategoriesPageable(Map<String, String> params);
}
