package com.coffee_shop.coffee_shop.dto.response;

public record TokenRefreshResponse(
        String token,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn) {
}