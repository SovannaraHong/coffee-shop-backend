package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.security.auth.AuthUser;
import io.jsonwebtoken.Claims;

import java.util.List;

public interface JwtService {

    // ---- Staff (access + refresh) ----
    String generateAccessToken(AuthUser user);

    String generateRefreshToken(AuthUser user);

    boolean isRefreshToken(String token);

    List<String> extractAuthorities(String token);

    // ---- Customer (single token, merged in from the old JwtUtil) ----
    String generateCustomerToken(Long customerId, String email);

    Long extractCustomerId(String token);

    // ---- Shared ----
    Claims extractClaims(String token);

    String extractUsername(String token);

    boolean isTokenValid(String token);
}