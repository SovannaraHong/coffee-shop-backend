package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.OrderCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.OrderResponse;
import com.coffee_shop.coffee_shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("api/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.findByCustomerId(customerId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping
    public ResponseEntity<PageDTO<OrderResponse>> getPagination(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(orderService.getPagination(params));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirm(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancel(id));
    }

    @PatchMapping("/{id}/preparing")
    public ResponseEntity<OrderResponse> markPreparing(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markPreparing(id));
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<OrderResponse> markReady(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markReady(id));
    }

    @PatchMapping("/{id}/delivering")
    public ResponseEntity<OrderResponse> markDelivering(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markDelivering(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<OrderResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.complete(id));
    }
}