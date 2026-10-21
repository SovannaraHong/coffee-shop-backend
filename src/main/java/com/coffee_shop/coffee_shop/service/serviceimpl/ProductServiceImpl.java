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
import com.coffee_shop.coffee_shop.repository.OrderDetailRepository;
import com.coffee_shop.coffee_shop.repository.ProductRepository;
import com.coffee_shop.coffee_shop.repository.RecipeRepository;
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
    private final RecipeRepository recipeRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final S3Service s3Service;

    private final AddonRepository addonRepository;

    private final CacheManager cacheManager;
    private final ProductCacheKeyGenerator keyGenerator;


    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Override
    @Transactional
    public ProductResponse create(ProductRequest productRequest) {
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

        // CHANGED: LinkedHashSet instead of Collectors.toSet() (HashSet) to keep insertion order
        Set<Variant> variants = productRequest.getVariants().stream()
                .map(v -> {
                    Variant variant = variantMapper.toEntity(v);
                    variant.setIsActive(v.getIsActive() != null ? v.getIsActive() : true);
                    variant.setProduct(product);
                    return variant;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        product.setVariants(variants);
        product.setAddons(resolveAddons(productRequest.getAddonIds()));
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }


    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Transactional
    @Override
    public ProductResponse update(Long id, ProductRequest productRequest) {
        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", id));
        Category category = categoryService.findById(productRequest.getCategoryId());

        // CHANGED: duplicate-name check is now case-insensitive (same as create())
        // and throws BadRequestException (400) instead of ResourceNotFoundException (404).
        if (!product.getName().equalsIgnoreCase(productRequest.getName())) {
            Optional<Product> duplicate = productRepository.findByNameIgnoreCase(productRequest.getName());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(product.getId())) {
                throw new BadRequestException(
                        "Product with name " + productRequest.getName() + " already exists");
            }
        }

        productMapper.updateEntity(product, productRequest);
        product.setCategory(category);
        product.setAddons(resolveAddons(productRequest.getAddonIds()));
        syncVariants(product, productRequest.getVariants());

        productRepository.save(product);
        productRepository.flush();

        // Reload from DB so the response reflects the true persisted state.
        // Without this, Hibernate returns the stale in-memory object and the
        // removed variants are still present in product.getVariants().
        Product fresh = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", id));
        return productMapper.toResponse(fresh);
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
                log.info("CACHE HIT: {}", key);
                return cached;
            }
        }
        log.info("CACHE MISS: {}", key);

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

        // Upload first: never destroy the old image before the new one is confirmed
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

    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Transactional
    @Override
    public ProductResponse changeFeatureStatus(Long id) {
        Product byId = findById(id);
        byId.setFeatured(!byId.getFeatured());
        return productMapper.toResponse(productRepository.save(byId));
    }


    @Transactional(readOnly = true)
    @Override
    public List<ProductResponse> findProductByCategoryId(Long id) {
        Category category = categoryService.findById(id);
        if (Objects.isNull(category)) {
            throw new ResourceNotFoundException("No Category found.");
        }
        List<Product> byCategoryId = productRepository.findByCategoryId(category.getId());
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
    // TODO WITH SELL TABLE
    @Override
    public List<ProductResponse> findBestSellingProducts() {
        // CHANGED: removed the unused productRepository.findAll() call (it loaded every product for nothing)
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

    // helper method

    private void syncVariants(Product product, List<VariantRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("A product needs at least one variant");
        }

        // 1. Validate before touching entities
        Set<String> seen = new HashSet<>();
        for (VariantRequest r : requests) {
            String sku = r.getSku().trim();
            if (!seen.add(sku.toLowerCase())) {
                throw new BadRequestException("Duplicate SKU in request: " + sku);
            }
            if (variantRepository.existsBySkuAndProductIdNot(sku, product.getId())) {
                throw new BadRequestException("SKU already used by another product: " + sku);
            }
        }

        Map<Long, Variant> byId = product.getVariants().stream()
                .collect(Collectors.toMap(Variant::getId, v -> v));
        Map<String, Variant> bySku = product.getVariants().stream()
                .filter(v -> v.getSku() != null)
                .collect(Collectors.toMap(v -> v.getSku().toLowerCase(), v -> v));

        // 2. Decide which existing variant each request maps to
        Map<VariantRequest, Variant> matched = new IdentityHashMap<>();
        Set<Long> keepIds = new HashSet<>();
        for (VariantRequest r : requests) {
            Variant v = null;
            if (r.getId() != null) {
                v = byId.get(r.getId());
                if (v == null) {
                    throw new ResourceNotFoundException(
                            "Variant " + r.getId() + " does not belong to product " + product.getId());
                }
            } else {
                v = bySku.get(r.getSku().trim().toLowerCase()); // fallback when id is missing
            }
            if (v != null) {
                matched.put(r, v);
                keepIds.add(v.getId());
            }
        }

        // 3. Handle removed variants
        //    - If a variant is referenced by an order_detail it CANNOT be deleted
        //      (Oracle FK_ORDER_DETAIL_VARIANT). Deactivate it instead so history is preserved.
        //    - If it only has a recipe (no orders), delete the recipe first, then the variant.
        Set<Variant> variantsToRemove = product.getVariants().stream()
                .filter(v -> !keepIds.contains(v.getId()))
                .collect(Collectors.toSet());

        for (Variant v : variantsToRemove) {
            if (orderDetailRepository.existsByProductVariantId(v.getId())) {
                // Has order history — cannot delete. Deactivate silently.
                v.setIsActive(false);
                keepIds.add(v.getId()); // keep it in the collection, just deactivated
            } else {
                // No order history — safe to delete. Remove recipe first.
                recipeRepository.findByProductVariantId(v.getId())
                        .ifPresent(recipeRepository::delete);
            }
        }

        if (!variantsToRemove.isEmpty()) {
            recipeRepository.flush();
        }

        product.getVariants().removeIf(v -> !keepIds.contains(v.getId()));
        variantRepository.flush();

        // 4. Update matched, insert the rest
        for (VariantRequest r : requests) {
            Variant v = matched.get(r);
            if (v != null) {
                v.setName(r.getName());
                v.setPrice(r.getPrice());
                v.setSku(r.getSku().trim());
                if (r.getIsActive() != null) v.setIsActive(r.getIsActive());
            } else {
                Variant created = variantMapper.toEntity(r);
                created.setIsActive(r.getIsActive() != null ? r.getIsActive() : true);
                created.setSku(r.getSku().trim());
                created.setProduct(product);
                product.getVariants().add(created);
            }
        }
    }

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