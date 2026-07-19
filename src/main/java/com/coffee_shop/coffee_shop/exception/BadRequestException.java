package com.coffee_shop.coffee_shop.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public static BadRequestException alreadyExits(String entity, Long id, String name) {
        return new BadRequestException(
                entity + " with id = " + id + ", name = " + name + " already exists"
        );
    }
}
