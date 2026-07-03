package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.CategoryMapper;
import com.coffee_shop.coffee_shop.repository.CategoryRepository;
import com.coffee_shop.coffee_shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getCategories() {

        List<Category> allCategory = categoryRepository.findAll();
        if (Objects.isNull(allCategory) || allCategory.isEmpty()) {
            throw new ResourceNotFoundException("No categories found");
        }
        return categoryMapper.toResponseList(allCategory);
    }

    @Override
    public Category getCategoryById(Long id) {
        return null;
    }

    @Override
    public Category createCategory(Category category) {
        return null;
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        return null;
    }

    @Override
    public void deleteCategory(Long id) {

    }

    @Override
    public Page<Category> getCategoriesPageable(Map<String, String> params) {
        return null;
    }
}
