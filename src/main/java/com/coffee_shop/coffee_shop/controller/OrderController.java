package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.OrderCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.OrderResponse;
import com.coffee_shop.coffee_shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("api/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── CREATE — customer places an order ─────────────────────────────────────
    @PreAuthorize("hasRole('CUSTOMER') or hasAuthority('ORDER_MANAGE')")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    // ── READ — single order (owner or staff) ──────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_READ', 'ORDER_MANAGE') or @accessGuard.ownsOrder(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    // ── READ — customer's own order history ───────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_READ', 'ORDER_MANAGE') or @accessGuard.isOwnCustomer(#customerId, authentication)")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.findByCustomerId(customerId));
    }

    // ── READ — staff: list all orders ─────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_READ', 'ORDER_MANAGE')")
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    // ── READ — staff: paginated order list ────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_READ', 'ORDER_MANAGE')")
    @GetMapping
    public ResponseEntity<PageDTO<OrderResponse>> getPagination(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(orderService.getPagination(params));
    }

    // ── UPDATE — staff: confirm order ─────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ORDER_MANAGE')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirm(id));
    }

    // ── UPDATE — customer cancels own order / staff cancels any ──────────────
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ORDER_MANAGE') or @accessGuard.ownsOrder(#id, authentication)")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancel(id));
    }

    // ── UPDATE — staff: mark as preparing ────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ORDER_MANAGE')")
    @PatchMapping("/{id}/preparing")
    public ResponseEntity<OrderResponse> markPreparing(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markPreparing(id));
    }

    // ── UPDATE — staff: mark as ready ────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ORDER_MANAGE')")
    @PatchMapping("/{id}/ready")
    public ResponseEntity<OrderResponse> markReady(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markReady(id));
    }

    // ── UPDATE — staff: mark as out for delivery ──────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ORDER_MANAGE')")
    @PatchMapping("/{id}/delivering")
    public ResponseEntity<OrderResponse> markDelivering(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markDelivering(id));
    }

    // ── UPDATE — staff: mark as completed ─────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ORDER_MANAGE')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<OrderResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.complete(id));
    }
}