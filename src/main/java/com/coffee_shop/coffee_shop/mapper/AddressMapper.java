package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.AddressCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.AddressUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddressResponse;
import com.coffee_shop.coffee_shop.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "customerId", source = "customer.id")
    AddressResponse toResponse(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "isDefault", source = "isDefault")
    Address toEntity(AddressCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AddressUpdateRequest request, @MappingTarget Address address);
}