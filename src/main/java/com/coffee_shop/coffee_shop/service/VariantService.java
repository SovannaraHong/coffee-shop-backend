package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.VariantRequest;
import com.coffee_shop.coffee_shop.dto.response.VariantResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface VariantService {

    VariantResponse create(Long productId, VariantRequest variantRequest);

    VariantResponse update(Long id, VariantRequest variantRequest);

    List<VariantResponse> getAll();

    Page<VariantResponse> getPagination(Map<String, String> params);

    VariantResponse findById(Long id);

    List<VariantResponse> findByProductId(Long productId);

    void delete(Long id);

    VariantResponse changeStatus(Long id);

    VariantResponse updatePrice(Long id, VariantRequest variantRequest);

}
