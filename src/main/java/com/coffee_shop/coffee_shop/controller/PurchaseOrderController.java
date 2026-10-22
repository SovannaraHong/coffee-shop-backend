package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.PurchaseOrderCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.PurchaseOrderUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PurchaseOrderResponse;
import com.coffee_shop.coffee_shop.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // ── READ — single ─────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('PURCHASE_ORDER_READ', 'PURCHASE_ORDER_MANAGE')")
    @GetMapping("{id}")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    // ── READ — pagination ─────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('PURCHASE_ORDER_READ', 'PURCHASE_ORDER_MANAGE')")
    @GetMapping("pagination")
    public ResponseEntity<Page<PurchaseOrderResponse>> getPagination(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(purchaseOrderService.getPagination(params));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('PURCHASE_ORDER_CREATE', 'PURCHASE_ORDER_MANAGE')")
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@RequestBody PurchaseOrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderService.createPurchaseOrder(request));
    }

    // ── UPDATE — edit draft ───────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('PURCHASE_ORDER_UPDATE', 'PURCHASE_ORDER_MANAGE')")
    @PutMapping("{id}")
    public ResponseEntity<PurchaseOrderResponse> update(@PathVariable Long id,
                                                        @RequestBody PurchaseOrderUpdateRequest request) {
        return ResponseEntity.ok(purchaseOrderService.updatePurchaseOrder(id, request));
    }

    // ── UPDATE — submit order to supplier ────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('PURCHASE_ORDER_UPDATE', 'PURCHASE_ORDER_MANAGE')")
    @PatchMapping("{id}/order")
    public ResponseEntity<PurchaseOrderResponse> order(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.orderPurchaseOrder(id));
    }

    // ── UPDATE — mark as received ─────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('PURCHASE_ORDER_UPDATE', 'PURCHASE_ORDER_MANAGE')")
    @PatchMapping("{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receive(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.receivePurchaseOrder(id));
    }

    // ── DELETE — cancel order ─────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('PURCHASE_ORDER_DELETE', 'PURCHASE_ORDER_MANAGE')")
    @PatchMapping("{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancelPurchaseOrder(id));
    }
}