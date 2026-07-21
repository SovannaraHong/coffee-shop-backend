package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.CategoryRequest;
import com.coffee_shop.coffee_shop.dto.response.CategoryDetailResponse;
import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.CategoryDetailMapper;
import com.coffee_shop.coffee_shop.mapper.CategoryMapper;
import com.coffee_shop.coffee_shop.repository.CategoryRepository;
import com.coffee_shop.coffee_shop.service.CategoryService;
import com.coffee_shop.coffee_shop.specification.category.CategoryFilter;
import com.coffee_shop.coffee_shop.specification.category.CategorySpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryDetailMapper categoryDetailMapper;

    @Override
    public List<CategoryResponse> getCategories() {

        List<Category> allCategory = categoryRepository.findAll();
        if (allCategory.isEmpty()) {
            throw new ResourceNotFoundException("No categories found");
        }
        return categoryMapper.toResponseList(allCategory);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id).
                orElseThrow(() -> ResourceNotFoundException.notFoundException("Category", id));
    }

    @Override
    public Category createCategory(Category category) {

        Optional<Category> exits = categoryRepository.findByNameIgnoreCase(category.getName());

        if (exits.isPresent()) {
            throw BadRequestException.alreadyExits("Category", exits.get().getId(), category.getName());
        }
        return categoryRepository.save(category);
    }


    @Override
    public Category update(Long id, CategoryRequest request) {
        Category categoryById = findById(id);

        if (!categoryById.getName().equalsIgnoreCase(request.getName())) {
            Optional<Category> exits = categoryRepository.findByNameIgnoreCase(request.getName());
            if (exits.isPresent()) {
                throw BadRequestException.alreadyExits("Category", exits.get().getId(), request.getName());
            }
        }

        categoryMapper.updateEntityFromRequest(request, categoryById);
        return categoryRepository.save(categoryById);
    }

    @Override
    public void delete(Long id) {
        Category byId = findById(id);
        categoryRepository.delete(byId);
    }

    @Override
    public Page<CategoryResponse> getCategoriesPageable(Map<String, String> params) {
        CategoryFilter filter = new CategoryFilter();
        if (params.containsKey("name")) filter.setName(params.get("name"));
        if (params.containsKey("id")) filter.setId(Long.parseLong(params.get("id")));
        CategorySpec categorySpec = new CategorySpec(filter);
        Pageable pageable = PageUtil.getPageable(params);

        return categoryRepository.findAll(categorySpec, pageable).map(categoryMapper::toResponse);
    }

    @Override
    public CategoryResponse updateImage(Long id, String imageUrl) {
        Category categoryId = findById(id);

        categoryId.setImageUrl(imageUrl);
        return categoryMapper.toResponse(categoryRepository.save(categoryId));


    }

    //TODO CHECK PRODUCT
    @Override
    public List<CategoryDetailResponse> findCategoryDetail(Long id) {
        Category categoryId = findById(id);
        return categoryDetailMapper.toResponseList(categoryId.getProducts());

    }

    @Override
    public CategoryResponse changeCategoryStatus(Long id) {
        Category category = findById(id);
        category.setIsActive(!category.getIsActive());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> findActiveCategories() {
        return categoryRepository.findAll().stream().filter(Category::getIsActive).map(categoryMapper::toResponse).toList();

    }
}
