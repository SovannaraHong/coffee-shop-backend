package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.PurchaseOrderCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.PurchaseOrderUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PurchaseOrderResponse;
import com.coffee_shop.coffee_shop.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@RequestBody PurchaseOrderCreateRequest request) {
        return ResponseEntity.ok(purchaseOrderService.createPurchaseOrder(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<PurchaseOrderResponse> update(@PathVariable Long id,
                                                        @RequestBody PurchaseOrderUpdateRequest request) {
        return ResponseEntity.ok(purchaseOrderService.updatePurchaseOrder(id, request));
    }

    @GetMapping("{id}")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @GetMapping("pagination")
    public ResponseEntity<Page<PurchaseOrderResponse>> getPagination(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(purchaseOrderService.getPagination(params));
    }

    @PatchMapping("{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancelPurchaseOrder(id));
    }

    @PatchMapping("{id}/order")
    public ResponseEntity<PurchaseOrderResponse> order(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.orderPurchaseOrder(id));
    }

    @PatchMapping("{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receive(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.receivePurchaseOrder(id));
    }
}