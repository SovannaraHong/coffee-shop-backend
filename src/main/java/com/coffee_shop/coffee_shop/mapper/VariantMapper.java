package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.VariantRequest;
import com.coffee_shop.coffee_shop.dto.response.VariantResponse;
import com.coffee_shop.coffee_shop.entity.Variant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VariantMapper {
    @Mapping(target = "productName", source = "product.name")
    VariantResponse toResponse(Variant variant);

    Variant toEntity(VariantRequest variantRequest);

    void updateEntity(@MappingTarget Variant variant, VariantRequest request);

}
