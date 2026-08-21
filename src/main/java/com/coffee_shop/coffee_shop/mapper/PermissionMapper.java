package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.PermissionUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PermissionResponse;
import com.coffee_shop.coffee_shop.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toResponse(Permission permission);

    void updateEntity(@MappingTarget Permission permission, PermissionUpdateRequest permissionUpdateRequest);
}