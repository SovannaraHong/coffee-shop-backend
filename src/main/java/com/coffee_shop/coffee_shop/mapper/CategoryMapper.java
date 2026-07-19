package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.CategoryRequest;
import com.coffee_shop.coffee_shop.dto.response.CategoryResponse;
import com.coffee_shop.coffee_shop.entity.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "image", source = "imageUrl")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @Mapping(target = "imageUrl", source = "image")
    Category toEntity(CategoryRequest request);

    //BeanMapping ---> it copy field in object
    // NullValuePropertyMappingStrategy.IGNORE ->If a field in the request is null, do NOT overwrite the existing value in the entity. like user submit null it not update data if in database have name
    //@MappingTarget-> it update one instead of create new object one
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
