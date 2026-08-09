package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.util.enums.PaymentStatus;

public class CutLuyStatusMapper {
    public static PaymentStatus toPaymentStatus(String cutluyStatus) {
        return switch (cutluyStatus) {
            case "paid" -> PaymentStatus.PAID;
            case "expired", "failed" -> PaymentStatus.FAILED;
            case "pending", "scanned" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }
}