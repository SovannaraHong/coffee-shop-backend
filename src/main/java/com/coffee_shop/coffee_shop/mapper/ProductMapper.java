package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.ProductRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    //    Take the value from the method parameter named category and put it into the target field named category.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntity(@MappingTarget Product target, ProductRequest request);

    //expression ->Run this Java code when mapping.
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "salePrice", expression = "java(calculateSalePrice(product.getPrice(), product.getDiscount()))")
    ProductResponse toResponse(Product product);


    default BigDecimal calculateSalePrice(BigDecimal price, BigDecimal discount) {
        if (price == null) return BigDecimal.ZERO;
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) return price;
        BigDecimal discountAmount = price.multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return price.subtract(discountAmount);
    }
}
