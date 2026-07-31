package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.PurchaseOrderResponse;
import com.coffee_shop.coffee_shop.entity.PurchaseOrder;
import com.coffee_shop.coffee_shop.entity.PurchaseOrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseOrderMapper {

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "details", source = "purchaseOrderDetails")
    PurchaseOrderResponse toResponse(PurchaseOrder entity);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    PurchaseOrderResponse.PurchaseOrderDetailResponse toDetailResponse(PurchaseOrderDetail detail);

    List<PurchaseOrderResponse.PurchaseOrderDetailResponse> toDetailResponseList(List<PurchaseOrderDetail> details);
}