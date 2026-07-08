package com.coffee_shop.coffee_shop.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException notFoundException(String entity, Long id) {
        return new ResourceNotFoundException(entity + " not found with id " + id);
    }
}