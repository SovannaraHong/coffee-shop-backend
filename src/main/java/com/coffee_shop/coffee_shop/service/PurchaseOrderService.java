package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.PurchaseOrderCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.PurchaseOrderUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PurchaseOrderResponse;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface PurchaseOrderService {

    PurchaseOrderResponse createPurchaseOrder(
            PurchaseOrderCreateRequest request
    );

    PurchaseOrderResponse updatePurchaseOrder(
            Long id,
            PurchaseOrderUpdateRequest request
    );

    PurchaseOrderResponse getPurchaseOrderById(Long id);

    Page<PurchaseOrderResponse> getPagination(
            Map<String, String> params
    );

    PurchaseOrderResponse cancelPurchaseOrder(Long id);

    PurchaseOrderResponse orderPurchaseOrder(Long id);

    PurchaseOrderResponse receivePurchaseOrder(Long id);
}