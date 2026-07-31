package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderUpdateRequest {

    private Long supplierId;

    @Valid
    private List<PurchaseOrderCreateRequest.PurchaseOrderDetailRequest> details;
}