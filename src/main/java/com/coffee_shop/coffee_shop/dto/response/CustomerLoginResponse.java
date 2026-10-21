package com.coffee_shop.coffee_shop.dto.response;

public record CustomerLoginResponse(
        String token,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        CustomerResponse customer) {
}