package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.response.PaymentResponse;
import com.coffee_shop.coffee_shop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentStatusController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> checkStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refreshPaymentStatus(paymentId));
    }
}