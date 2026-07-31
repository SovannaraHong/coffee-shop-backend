package com.coffee_shop.coffee_shop.exception;

import java.math.BigDecimal;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public static BadRequestException alreadyExits(String entity, Long id, String name) {
        return new BadRequestException(
                entity + " with id = " + id + ", name = " + name + " already exists"
        );
    }

    public static BadRequestException insufficientStock(String ingredientName, BigDecimal available, BigDecimal requested) {
        return new BadRequestException(
                String.format("Insufficient stock for '%s': available %.2f, requested %.2f",
                        ingredientName, available, requested)
        );
    }
}
