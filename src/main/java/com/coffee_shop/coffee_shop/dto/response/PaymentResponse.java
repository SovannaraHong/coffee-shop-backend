package com.coffee_shop.coffee_shop.dto.response;

import com.coffee_shop.coffee_shop.util.enums.PaymentMethod;
import com.coffee_shop.coffee_shop.util.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        String transactionRef,
        LocalDateTime paidAt
) {
}