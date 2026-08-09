package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.entity.Order;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.repository.OrderRepository;
import com.coffee_shop.coffee_shop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @PostMapping("/khqr/{orderId}")
    public ResponseEntity<PaymentService.KhqrPaymentResult> initiateKhqrPayment(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Order", orderId));

        PaymentService.KhqrPaymentResult result =
                paymentService.createKhqrPayment(order, order.getFinalAmount());

        return ResponseEntity.ok(result);
    }
}