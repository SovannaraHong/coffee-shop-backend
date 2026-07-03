package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<CategoryResponse> getCategories();

    Category getCategoryById(Long id);

    Category createCategory(Category category);

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);

    Page<Category> getCategoriesPageable(Map<String, String> params);
}
