package com.coffee_shop.coffee_shop.specification.payment;

import com.coffee_shop.coffee_shop.util.enums.PaymentMethod;
import com.coffee_shop.coffee_shop.util.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentFilter {
    private Long id;
    private Long orderId;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionRef;
}