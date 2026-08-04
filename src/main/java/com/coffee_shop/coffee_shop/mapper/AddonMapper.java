package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.AddonCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonResponse;
import com.coffee_shop.coffee_shop.entity.Addon;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddonMapper {
    AddonResponse toAddonResponse(Addon addon);

    Addon toEntity(AddonCreateRequest request);

    void update(@MappingTarget Addon addon, AddonCreateRequest request);
}
