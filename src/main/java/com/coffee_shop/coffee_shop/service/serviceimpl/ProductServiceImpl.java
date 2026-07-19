package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.mapper.ProductMapper;
import com.coffee_shop.coffee_shop.repository.ProductRepository;
import com.coffee_shop.coffee_shop.service.CategoryService;
import com.coffee_shop.coffee_shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    @Override
    public ProductResponse create(ProductRequest productRequest) {
        return null;
    }

    @Override
    public ProductResponse update(Long id, ProductRequest productRequest) {
        return null;
    }

    @Override
    public List<ProductResponse> getAll() {
        return List.of();
    }

    @Override
    public Page<ProductResponse> getPagination(Map<String, String> params) {
        return null;
    }

    @Override
    public Product findById(Long id) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public ProductResponse updateImage(Long id, String imageUrl) {
        return null;
    }

    @Override
    public ProductResponse changeProductStatus(Long id) {
        return null;
    }

    @Override
    public List<ProductResponse> findProductByCategoryId(Long id) {
        return List.of();
    }

    @Override
    public List<ProductResponse> findFeaturedProducts() {
        return List.of();
    }

    @Override
    public List<ProductResponse> findBestSellingProducts() {
        return List.of();
    }

    @Override
    public List<ProductResponse> findNewestProducts() {
        return List.of();
    }

//    @Transactional
//    @Override
//    public ProductResponse create(ProductRequest productRequest) {
//        boolean name = productRepository.existsByName(productRequest.getName());
//        if (name) {
//            throw new ResourceNotFoundException("Product with name " + productRequest.getName() + " already exists");
//        }
//        Category cateId = categoryService.findById(productRequest.getCategoryId());
//        Product entity = productMapper.toEntity(productRequest);
//        entity.setCategory(cateId);
//        return productMapper.toResponse(productRepository.save(entity));
//
//    }
//
//    @Override
//    public ProductResponse update(Long id, ProductRequest productRequest) {
//        Product proId = findById(id);
//        Category cateId = categoryService.findById(productRequest.getCategoryId());
//        if (!proId.getName().equals(productRequest.getName())) {
//            boolean b = productRepository.existsByName(productRequest.getName());
//            if (b) {
//                throw new
//                        ResourceNotFoundException("Product with name " + productRequest.getName() +
//                        " already exists");
//            }
//
//        }
//
//        productMapper.updateEntity(proId, productRequest);
//        proId.setCategory(cateId);
//
//        return productMapper.toResponse(productRepository.save(proId));
//    }
//
//    @Override
//    public List<ProductResponse> getAll() {
//        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
//                .map(productMapper::toResponse)
//                .toList();
//    }
//
//    @Override
//    public Page<ProductResponse> getPagination(Map<String, String> params) {
//        ProductFilter productFilter = new ProductFilter();
//        if (params.containsKey("name")) productFilter.setName(params.get("name"));
//        if (params.containsKey("id")) productFilter.setId(Long.parseLong(params.get("id")));
//        ProductSpec productSpec = new ProductSpec(productFilter);
//        Pageable pageable = PageUtil.getPageable(params);
//        return productRepository.findAll(productSpec, pageable).map(productMapper::toResponse);
//
//    }
//
//    @Override
//    public Product findById(Long id) {
//        return productRepository.findById(id)
//                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", id));
//
//    }
//
//    @Override
//    public void delete(Long id) {
//        Product byId = findById(id);
//        productRepository.delete(byId);
//
//
//    }
//
//    @Transactional
//    @Override
//    public ProductResponse updateImage(Long id, String imageUrl) {
//        Product product = findById(id);
//        product.setImageUrl(imageUrl);
//        return productMapper.toResponse(productRepository.save(product));
//    }
//
//    @Override
//    public ProductResponse changeProductStatus(Long id) {
//        Product byId = findById(id);
//        byId.setIsActive(!byId.getIsActive());
//        return productMapper.toResponse(productRepository.save(byId));
//    }
//
//    @Override
//    public List<ProductResponse> findProductByCategoryId(Long id) {
//
//        Category categoryId = categoryService.findById(id);
//        if (Objects.isNull(categoryId)) {
//            throw new ResourceNotFoundException("No Category found.");
//        }
//        List<Product> byCategoryId = productRepository.findByCategoryId(categoryId.getId());
//        if (byCategoryId.isEmpty()) {
//            throw new ResourceNotFoundException("Not found product.");
//        }
//        return byCategoryId.stream().map(productMapper::toResponse).toList();
//
//    }
//
//    @Override
//    public List<ProductResponse> findFeaturedProducts() {
////        List<ProductResponse> result = productRepository
////                .findAll()
////                .stream()
////                .filter(Product::getFeatured)
////                .map(productMapper::toResponse)
////                .toList();
////
////        if (result.isEmpty()) {
////            throw new ResourceNotFoundException("No featured products found");
////        }
////
////        return result;
//        return null;
//    }
//
//    @Override
//    public List<ProductResponse> findBestSellingProducts() {
//        List<Product> all = productRepository.findAll();
//
//        return List.of();
//    }
//
//    @Override
//    public List<ProductResponse> findNewestProducts() {
//        return productRepository
//                .findTop10ByOrderByCreatedAtDesc()
//                .stream()
//                .map(productMapper::toResponse)
//                .toList();
//    }

}
