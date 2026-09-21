package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.request.VariantRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Addon;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.entity.Variant;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.FileValidationException;
import com.coffee_shop.coffee_shop.exception.ImageUploadException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.ProductMapper;
import com.coffee_shop.coffee_shop.mapper.VariantMapper;
import com.coffee_shop.coffee_shop.repository.AddonRepository;
import com.coffee_shop.coffee_shop.repository.ProductRepository;
import com.coffee_shop.coffee_shop.repository.VariantRepository;
import com.coffee_shop.coffee_shop.service.CategoryService;
import com.coffee_shop.coffee_shop.service.ProductService;
import com.coffee_shop.coffee_shop.service.S3Service;
import com.coffee_shop.coffee_shop.specification.product.ProductFilter;
import com.coffee_shop.coffee_shop.specification.product.ProductSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import com.coffee_shop.coffee_shop.util.ProductCacheKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {


    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp"
    );

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final VariantMapper variantMapper;
    private final CategoryService categoryService;
    private final VariantRepository variantRepository;
    private final S3Service s3Service;

    private final AddonRepository addonRepository;

    private final CacheManager cacheManager;
    private final ProductCacheKeyGenerator keyGenerator;


    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Override
    @Transactional
    public ProductResponse create(ProductRequest productRequest) {
//        if (productRepository.existsByName(productRequest.getName())) {
//            throw BadRequestException.alreadyExits("Product", productRequest., productRequest.getName());
//        }
        Optional<Product> exits = productRepository.findByNameIgnoreCase(productRequest.getName());

        if (exits.isPresent()) {
            throw BadRequestException.alreadyExits("Product", exits.get().getId(), productRequest.getName());
        }

        // Collect ALL duplicate SKUs, not just the first one
        List<String> duplicateSkus = productRequest.getVariants().stream()
                .map(VariantRequest::getSku)
                .filter(Objects::nonNull)
                .filter(variantRepository::existsBySku)
                .toList();

        if (!duplicateSkus.isEmpty()) {
            throw new BadRequestException(
                    "The following SKU(s) already exist: " + String.join(", ", duplicateSkus)
            );
        }

        Category category = categoryService.findById(productRequest.getCategoryId());
        Product product = productMapper.toEntity(productRequest);
        product.setCategory(category);
        product.setIsActive(productRequest.getIsActive() != null ? productRequest.getIsActive() : true);
        product.setFeatured(productRequest.getFeatured() != null ? productRequest.getFeatured() : true);

        Set<Variant> variants = productRequest.getVariants().stream()
                .map(v -> {
                    Variant variant = variantMapper.toEntity(v);
                    variant.setIsActive(v.getIsActive() != null ? v.getIsActive() : true);
                    variant.setProduct(product);
                    return variant;
                })
                .collect(Collectors.toSet());

        product.setVariants(variants);
        product.setAddons(resolveAddons(productRequest.getAddonIds()));
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }


    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Transactional
    @Override
    public ProductResponse update(Long id, ProductRequest productRequest) {
        Product proId = findById(id);
        Category cateId = categoryService.findById(productRequest.getCategoryId());
        if (!proId.getName().equals(productRequest.getName())) {
            boolean b = productRepository.existsByName(productRequest.getName());
            if (b) {
                throw new
                        ResourceNotFoundException("Product with name " + productRequest.getName() +
                        " already exists");
            }

        }

        productMapper.updateEntity(proId, productRequest);
        proId.setCategory(cateId);
        proId.setAddons(resolveAddons(productRequest.getAddonIds()));

        return productMapper.toResponse(productRepository.save(proId));

    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        String key = "all";
        Cache cache = cacheManager.getCache("productList");

        if (cache != null) {
            List<ProductResponse> cached = cache.get(key, List.class);
            if (cached != null) {
                return cached;
            }
        }
        List<ProductResponse> result =
                productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                        .map(productMapper::toResponse)
                        .toList();
        if (cache != null) {
            cache.put(key, result);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<ProductResponse> getPagination(Map<String, String> params) {
        String key = keyGenerator.generate(params);
        Cache cache = cacheManager.getCache("productPagination");
        if (cache != null) {
            PageDTO<ProductResponse> cached = cache.get(key, PageDTO.class);
            if (cached != null) {
                log.info("✅ CACHE HIT: {}", key);
                return cached;
            }
        }
        log.info(" CACHE MISS: {}", key);

        ProductFilter productFilter = new ProductFilter();
        if (params.containsKey("name")) productFilter.setName(params.get("name"));
        if (params.containsKey("id")) productFilter.setId(Long.parseLong(params.get("id")));
        ProductSpec productSpec = new ProductSpec(productFilter);
        Pageable pageable = PageUtil.getPageable(params);
        Page<ProductResponse> page = productRepository.findAll(productSpec, pageable).map(productMapper::toResponse);

        PageDTO<ProductResponse> result = new PageDTO<>(page);

        if (cache != null) {
            cache.put(key, result);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", id));

    }

    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Transactional
    @Override
    public void delete(Long id) {
        Product byId = findById(id);
        productRepository.delete(byId);


    }

    @Override
    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Transactional
    public ProductResponse uploadProductImage(Long id, MultipartFile file) throws IOException {
        validateImageFile(file);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        String oldUrl = product.getImageUrl();

        // Upload first — never destroy the old image before the new one is confirmed
        String newUrl;
        try {
            newUrl = s3Service.uploadFile(file, "product_images");
        } catch (IOException e) {
            log.error("Failed to upload image for product {}", id, e);
            throw new ImageUploadException("Failed to upload image, please try again", e);
        }

        product.setImageUrl(newUrl);
        Product saved;
        try {
            saved = productRepository.save(product);
        } catch (Exception e) {
            log.error("Failed to save image URL for product {}, cleaning up orphaned upload", id, e);
            safeDelete(newUrl);
            throw e;
        }

        // Only now clean up the old image, and only if it's actually ours
        if (oldUrl != null && s3Service.isManagedUrl(oldUrl)) {
            safeDelete(oldUrl);
        }

        return productMapper.toResponse(saved);
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

    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Transactional
    @Override
    public ProductResponse changeProductStatus(Long id) {
        Product byId = findById(id);
        byId.setIsActive(!byId.getIsActive());
        return productMapper.toResponse(productRepository.save(byId));
    }


    @Transactional(readOnly = true)
    @Override
    public List<ProductResponse> findProductByCategoryId(Long id) {

        Category categoryId = categoryService.findById(id);
        if (Objects.isNull(categoryId)) {
            throw new ResourceNotFoundException("No Category found.");
        }
        List<Product> byCategoryId = productRepository.findByCategoryId(categoryId.getId());
        if (byCategoryId.isEmpty()) {
            throw new ResourceNotFoundException("Not found product.");
        }
        return byCategoryId.stream().map(productMapper::toResponse).toList();

    }


    @Transactional(readOnly = true)
    @Override
    public List<ProductResponse> findFeaturedProducts() {
        List<ProductResponse> result = productRepository
                .findAll()
                .stream()
                .filter(Product::getFeatured)
                .map(productMapper::toResponse)
                .toList();

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("No featured products found");
        }

        return result;
    }

    @Transactional(readOnly = true)
    //TODO WITH SELL TABLE
    @Override
    public List<ProductResponse> findBestSellingProducts() {
        List<Product> all = productRepository.findAll();

        return List.of();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductResponse> findNewestProducts() {
        return productRepository
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    //helper method
    private Set<Addon> resolveAddons(Set<Long> addonIds) {
        if (addonIds == null || addonIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Addon> found = addonRepository.findAllById(addonIds);
        if (found.size() != addonIds.size()) {
            Set<Long> foundIds = found.stream().map(Addon::getId).collect(Collectors.toSet());
            Set<Long> missing = new HashSet<>(addonIds);
            missing.removeAll(foundIds);
            throw new ResourceNotFoundException("Addon(s) not found with id(s): " + missing);
        }
        List<Addon> inactive = found
                .stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsActive())).toList();

        if (!inactive.isEmpty()) {
            String names = inactive.stream().map(Addon::getName).collect(Collectors.joining(", "));
            throw new BadRequestException("The following addon(s) are inactive and cannot be assigned: " + names);

        }
        return new HashSet<>(found);
    }

}
