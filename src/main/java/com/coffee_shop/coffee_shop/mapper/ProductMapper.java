package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.dto.response.VariantResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = VariantMapper.class)
public abstract class ProductMapper {

    @Autowired
    protected VariantMapper variantMapper;

    //    Take the value from the method parameter named category and put it into the target field named category.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    public abstract Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "addons", ignore = true)
    public abstract void updateEntity(@MappingTarget Product target, ProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "variants", ignore = true) // handled manually in toResponse()
    protected abstract ProductResponse toResponseInternal(Product product);

    /**
     * Maps a Product to ProductResponse, only including ACTIVE variants.
     * Inactive variants are kept in DB for order-history integrity but must
     * not be surfaced to callers.
     */
    public ProductResponse toResponse(Product product) {
        ProductResponse response = toResponseInternal(product);
        if (product.getVariants() != null) {
            List<VariantResponse> activeVariants = product.getVariants().stream()
                    .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                    .map(variantMapper::toResponse)
                    .collect(Collectors.toList());
            response.setVariants(activeVariants);
        }
        return response;
    }
}
