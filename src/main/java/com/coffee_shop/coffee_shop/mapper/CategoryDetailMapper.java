package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.CategoryDetailResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CategoryDetailMapper {
    List<CategoryDetailResponse> toResponseList(Set<Product> products);
}
