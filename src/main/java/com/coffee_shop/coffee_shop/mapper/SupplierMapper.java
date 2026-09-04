package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.request.SupplierCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.SupplierUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.SupplierResponse;
import com.coffee_shop.coffee_shop.entity.Supplier;
import com.coffee_shop.coffee_shop.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "userToString")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "userToString")
    SupplierResponse toSupplierResponse(Supplier supplier);

    Supplier toEntity(SupplierCreateRequest request);

    void toUpdate(@MappingTarget Supplier supplier, SupplierUpdateRequest request);

    @Named("userToString")
    default String userToString(User user) {
        if (user == null) {
            return null;
        }
        return user.getFullName(); // swap for getEmail()/getFullName() if that's what you want shown
    }
}