package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.AddonCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonResponse;

import java.util.List;

public interface AddonService {
    AddonResponse create(AddonCreateRequest request);

    AddonResponse update(Long id, AddonCreateRequest request);

    void delete(Long id);

    AddonResponse findById(Long id);

    List<AddonResponse> getAll();

    AddonResponse changeStatus(Long id);
}