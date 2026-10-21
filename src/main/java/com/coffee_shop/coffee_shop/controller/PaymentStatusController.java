package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.response.PaymentResponse;
import com.coffee_shop.coffee_shop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentStatusController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAuthority('PAYMENT_MANAGE') or @accessGuard.ownsPayment(#paymentId, authentication)")
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> checkStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refreshPaymentStatus(paymentId));
    }

    @PreAuthorize("hasAuthority('PAYMENT_MANAGE')")
    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    @PreAuthorize("hasAuthority('PAYMENT_MANAGE')")
    @GetMapping
    public ResponseEntity<PageDTO<PaymentResponse>> getPagination(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(paymentService.getPagination(params));
    }
}