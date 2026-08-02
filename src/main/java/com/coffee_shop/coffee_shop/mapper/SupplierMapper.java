package com.coffee_shop.coffee_shop.mapper;


import com.coffee_shop.coffee_shop.dto.request.SupplierCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.SupplierUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.SupplierResponse;
import com.coffee_shop.coffee_shop.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierResponse toSupplierResponse(Supplier supplier);

    Supplier toEntity(SupplierCreateRequest request);

    void toUpdate(@MappingTarget Supplier supplier, SupplierUpdateRequest request);


}
