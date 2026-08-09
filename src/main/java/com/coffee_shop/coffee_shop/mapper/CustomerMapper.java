package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;
import com.coffee_shop.coffee_shop.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponse toResponse(Customer customer);
}