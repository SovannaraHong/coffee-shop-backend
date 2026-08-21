package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.UserResponse;
import com.coffee_shop.coffee_shop.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.name", target = "roleName")
    UserResponse toResponse(User user);
}