package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.ProductMapper;
import com.coffee_shop.coffee_shop.repository.ProductRepository;
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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    @Transactional
    @Override
    public ProductResponse create(ProductRequest productRequest) {
        Boolean name = productRepository.existsByName(productRequest.getName());
        if (name) {
            throw new ResourceNotFoundException("Product with name " + productRequest.getName() + " already exists");
        }
        Category cateId = categoryService.findById(productRequest.getCategoryId());
        Product entity = productMapper.toEntity(productRequest);
        entity.setCategory(cateId);
        return productMapper.toResponse(productRepository.save(entity));

    }

    @Override
    public ProductResponse update(Long id, ProductRequest productRequest) {
        Product proId = findById(id);
        Category cateId = categoryService.findById(productRequest.getCategoryId());
        if (!proId.getName().equals(productRequest.getName())) {
            boolean b = productRepository.existsByName(productRequest.getName());
            if (b) {
                throw new ResourceNotFoundException("Product with name " + productRequest.getName() + " already exists");
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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", id));
        return product;

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
        product.setImage(imageUrl);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse changeProductStatus(Long id) {
        Product byId = findById(id);
        byId.setStatus(!byId.getStatus());
        return productMapper.toResponse(productRepository.save(byId));
    }

    @Override
    public ProductResponse findProductByCategoryId(Long id) {
        return null;
    }

    @Override
    public ProductResponse findFeaturedProducts() {
        return null;
    }
}
