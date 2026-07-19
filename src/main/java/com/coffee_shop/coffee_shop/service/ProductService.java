package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProductService {

    ProductResponse create(ProductRequest productRequest);

    ProductResponse update(Long id, ProductRequest productRequest);

    List<ProductResponse> getAll();

    Page<ProductResponse> getPagination(Map<String, String> params);

    Product findById(Long id);

    void delete(Long id);

    ProductResponse updateImage(Long id, String imageUrl);


    ProductResponse changeProductStatus(Long id);

    ProductResponse findProductByCategoryId(Long id);

    ProductResponse findFeaturedProducts();

}
