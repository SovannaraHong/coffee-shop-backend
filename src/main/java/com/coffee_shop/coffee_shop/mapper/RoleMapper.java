package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.RoleUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.RoleResponse;
import com.coffee_shop.coffee_shop.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = PermissionMapper.class)
public interface RoleMapper {
    RoleResponse toResponse(Role role);

    void updateEntity(@MappingTarget Role role, RoleUpdateRequest request);
}