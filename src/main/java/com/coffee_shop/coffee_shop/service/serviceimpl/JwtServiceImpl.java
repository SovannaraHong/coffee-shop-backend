package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.security.auth.AuthUser;
import com.coffee_shop.coffee_shop.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ---- Staff ----

    @Override
    public String generateAccessToken(AuthUser user, String sessionId, String deviceFingerprint) {
        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "STAFF_ACCESS");
        claims.put("authorities", authorities);
        claims.put("sessionId", sessionId);
        claims.put("device", deviceFingerprint);

        return buildToken(user.getUsername(), accessTokenExpirationMs, claims);
    }

    @Override
    public String generateRefreshToken(AuthUser user, String sessionId, String deviceFingerprint) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "STAFF_REFRESH");
        claims.put("sessionId", sessionId);
        claims.put("device", deviceFingerprint);

        return buildToken(user.getUsername(), refreshTokenExpirationMs, claims);
    }

    @Override
    public boolean isRefreshToken(String token) {
        return "STAFF_REFRESH".equals(extractClaims(token).get("type", String.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        return (List<String>) extractClaims(token).get("authorities", List.class);
    }

    @Override
    public String extractSessionId(String token) {
        return extractClaims(token).get("sessionId", String.class);
    }

    @Override
    public String extractDeviceFingerprint(String token) {
        return extractClaims(token).get("device", String.class);
    }

    // ---- Customer (merged in from the old JwtUtil) ----

    @Override
    public String generateCustomerToken(Long customerId, String email) {
        return buildToken(email, accessTokenExpirationMs, Map.of(
                "customerId", customerId,
                "type", "CUSTOMER"
        ));
    }

    @Override
    public Long extractCustomerId(String token) {
        return extractClaims(token).get("customerId", Long.class);
    }

    // ---- Shared ----

    private String buildToken(String subject, long expiryMs, Map<String, Object> claims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            return extractClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}