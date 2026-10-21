package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.response.PaymentResponse;
import com.coffee_shop.coffee_shop.entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    record KhqrPaymentResult(PaymentResponse payment, String checkoutUrl, String qrString) {
    }

    List<PaymentResponse> getAll();

    PageDTO<PaymentResponse> getPagination(Map<String, String> params);

    KhqrPaymentResult createKhqrPayment(Order order, BigDecimal amount);

    PaymentResponse getPayment(Long paymentId);

    PaymentResponse refreshPaymentStatus(Long paymentId);
}