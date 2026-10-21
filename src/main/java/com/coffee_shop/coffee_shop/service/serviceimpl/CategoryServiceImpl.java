package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.CategoryRequest;
import com.coffee_shop.coffee_shop.dto.response.CategoryDetailResponse;
import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.FileValidationException;
import com.coffee_shop.coffee_shop.exception.ImageUploadException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.CategoryDetailMapper;
import com.coffee_shop.coffee_shop.mapper.CategoryMapper;
import com.coffee_shop.coffee_shop.repository.CategoryRepository;
import com.coffee_shop.coffee_shop.service.CategoryService;
import com.coffee_shop.coffee_shop.service.S3Service;
import com.coffee_shop.coffee_shop.specification.category.CategoryFilter;
import com.coffee_shop.coffee_shop.specification.category.CategorySpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryDetailMapper categoryDetailMapper;
    private final S3Service s3Service;

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp"
    );

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getCategories() {

        List<Category> allCategory = categoryRepository.findAll();
        if (allCategory.isEmpty()) {
            throw new ResourceNotFoundException("No categories found");
        }
        return categoryMapper.toResponseList(allCategory);
    }

    @Transactional(readOnly = true)
    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id).
                orElseThrow(() -> ResourceNotFoundException.notFoundException("Category", id));
    }

    @Transactional
    @Override
    public Category createCategory(Category category) {

        Optional<Category> exits = categoryRepository.findByNameIgnoreCase(category.getName());

        if (exits.isPresent()) {
            throw BadRequestException.alreadyExits("Category", exits.get().getId(), category.getName());
        }
        return categoryRepository.save(category);
    }

    @Transactional
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

    @Transactional
    @Override
    public void delete(Long id) {
        Category byId = findById(id);
        categoryRepository.delete(byId);
    }

    @Transactional(readOnly = true)
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
    public CategoryResponse uploadImage(Long id, MultipartFile file) throws IOException {
        validateImageFile(file);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        String oldUrl = category.getImageUrl();

        // Upload first — never destroy the old image before the new one is confirmed
        String newUrl;
        try {
            newUrl = s3Service.uploadFile(file, "product_images");
        } catch (IOException e) {
            log.error("Failed to upload image for product {}", id, e);
            throw new ImageUploadException("Failed to upload image, please try again", e);
        }

        category.setImageUrl(newUrl);
        Category saved;
        try {
            saved = categoryRepository.save(category);
        } catch (Exception e) {
            log.error("Failed to save image URL for product {}, cleaning up orphaned upload", id, e);
            safeDelete(newUrl);
            throw e;
        }

        // Only now clean up the old image, and only if it's actually ours
        if (oldUrl != null && s3Service.isManagedUrl(oldUrl)) {
            safeDelete(oldUrl);
        }

        return categoryMapper.toResponse(saved);
    }

    private void safeDelete(String url) {
        try {
            s3Service.deleteFile(url);
        } catch (Exception e) {
            log.warn("Failed to delete image at {}", url, e);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new FileValidationException("File exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileValidationException("Only PNG, JPEG, or WEBP images are allowed");
        }
    }

    @Transactional(readOnly = true)
    //TODO CHECK PRODUCT
    @Override
    public List<CategoryDetailResponse> findCategoryDetail(Long id) {
        Category categoryId = findById(id);
        return categoryDetailMapper.toResponseList(categoryId.getProducts());

    }

    @Transactional
    @Override
    public CategoryResponse changeCategoryStatus(Long id) {
        Category category = findById(id);
        category.setIsActive(!category.getIsActive());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public List<CategoryResponse> findActiveCategories() {
        return categoryRepository.findAll().stream().filter(Category::getIsActive).map(categoryMapper::toResponse).toList();

    }
}
