package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.ReviewResponse;
import com.coffee_shop.coffee_shop.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(target = "customerName",
            expression = "java(review.getCustomer().getFirstName() + \" \" + review.getCustomer().getLastName())")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    ReviewResponse toResponse(Review review);
}