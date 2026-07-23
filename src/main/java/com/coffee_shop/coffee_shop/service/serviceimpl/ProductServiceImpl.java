package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.request.VariantRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.entity.Variant;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.ProductMapper;
import com.coffee_shop.coffee_shop.mapper.VariantMapper;
import com.coffee_shop.coffee_shop.repository.ProductRepository;
import com.coffee_shop.coffee_shop.repository.VariantRepository;
import com.coffee_shop.coffee_shop.service.CategoryService;
import com.coffee_shop.coffee_shop.service.ProductService;
import com.coffee_shop.coffee_shop.specification.product.ProductFilter;
import com.coffee_shop.coffee_shop.specification.product.ProductSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final VariantMapper variantMapper;
    private final CategoryService categoryService;
    private final VariantRepository variantRepository;


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
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

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

        return productMapper.toResponse(productRepository.save(proId));
    }

    @Override
    public List<ProductResponse> getAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public Page<ProductResponse> getPagination(Map<String, String> params) {
        ProductFilter productFilter = new ProductFilter();
        if (params.containsKey("name")) productFilter.setName(params.get("name"));
        if (params.containsKey("id")) productFilter.setId(Long.parseLong(params.get("id")));
        ProductSpec productSpec = new ProductSpec(productFilter);
        Pageable pageable = PageUtil.getPageable(params);
        return productRepository.findAll(productSpec, pageable).map(productMapper::toResponse);

    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", id));

    }

    @Override
    public void delete(Long id) {
        Product byId = findById(id);
        productRepository.delete(byId);


    }

    @Transactional
    @Override
    public ProductResponse updateImage(Long id, String imageUrl) {
        Product product = findById(id);
        product.setImageUrl(imageUrl);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse changeProductStatus(Long id) {
        Product byId = findById(id);
        byId.setIsActive(!byId.getIsActive());
        return productMapper.toResponse(productRepository.save(byId));
    }

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

    //TODO WITH SELL TABLE
    @Override
    public List<ProductResponse> findBestSellingProducts() {
        List<Product> all = productRepository.findAll();

        return List.of();
    }

    @Override
    public List<ProductResponse> findNewestProducts() {
        return productRepository
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

}
