package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.PermissionCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.PermissionUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse create(PermissionCreateRequest request);

    PermissionResponse update(Long id, PermissionUpdateRequest request);

    void delete(Long id);

    List<PermissionResponse> getAll();
}