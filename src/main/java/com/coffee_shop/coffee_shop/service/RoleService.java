package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.RoleCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.RoleUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse create(RoleCreateRequest request);

    RoleResponse assignPermission(Long roleId, Long permissionId);

    RoleResponse removePermission(Long roleId, Long permissionId);

    RoleResponse update(RoleUpdateRequest request, Long roleId);

    List<RoleResponse> getAll();

    RoleResponse findById(Long id);

    void delete(Long id);
}