package com.coffee_shop.coffee_shop.security.keycloak;

import com.coffee_shop.coffee_shop.dto.response.TokenRefreshResponse;
import com.coffee_shop.coffee_shop.entity.Permission;
import com.coffee_shop.coffee_shop.entity.User;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.repository.UserRepository;
import com.coffee_shop.coffee_shop.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Staff login through Keycloak: password + authenticator code (TOTP).
 */
@Slf4j
@Service
public class StaffAuthService {

    public record StaffLoginResponse(String accessToken, String refreshToken, String tokenType,
                                     long expiresIn, long refreshExpiresIn, Map<String, Object> user) {
    }

    private final RestClient rest;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;
    private final KeyCloakAdminService keycloakAdmin;
    private final S3Service s3Service;

    public StaffAuthService(@Value("${keycloak.base-url}") String baseUrl,
                            @Value("${keycloak.realm}") String realm,
                            @Value("${keycloak.staff-login-client-id}") String clientId,
                            @Value("${keycloak.staff-login-client-secret}") String clientSecret,
                            JwtDecoder jwtDecoder,
                            UserRepository userRepository,
                            KeyCloakAdminService keycloakAdmin,
                            S3Service s3Service) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
        this.keycloakAdmin = keycloakAdmin;
        this.s3Service = s3Service;
    }

    /**
     * True when the request has no code yet. Rejects staff who never set up an authenticator.
     */
    public boolean otpRequired(String email, String otp) {
        if (otp != null && !otp.isBlank()) return false;
        if (!keycloakAdmin.hasOtp(email)) {
            throw new BadRequestException(
                    "Two-step verification is not set up for this account. "
                            + "Open the setup link from your invitation email first.");
        }
        return true;
    }

    public StaffLoginResponse login(String email, String password, String otp) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", email);
        form.add("password", password);
        if (otp != null && !otp.isBlank()) form.add("totp", otp.trim());

        Map<String, Object> tokens = requestTokens(form, "Invalid email, password or code");
        Jwt jwt = jwtDecoder.decode((String) tokens.get("access_token"));

        // only staff may use this endpoint
        var staff = userRepository.findByKeycloakId(jwt.getSubject()).orElse(null);
        if (staff == null) {
            endSession((String) tokens.get("refresh_token"));
            throw new BadRequestException("Invalid email, password or code");
        }
        if (!Boolean.TRUE.equals(staff.getIsActive())) {          // adjust to your getter
            endSession((String) tokens.get("refresh_token"));
            throw new BadRequestException("This account has been deactivated");
        }

//        Map<String, Object> user = Map.of(
//                "email", String.valueOf(jwt.getClaimAsString("email")),
//                "name", String.valueOf(jwt.getClaimAsString("name")));
//
        Map<String, Object> user = new HashMap<>();   // Map.of() crashes on null values
        user.put("email", jwt.getClaimAsString("email"));
        user.put("name", jwt.getClaimAsString("name"));
        user.put("fullName", jwt.getClaimAsString("name"));   // frontend reads fullName
        user.put("avatarUrl", staff.getAvatarUrl());
        user.put("roleName", staff.getRole() != null ? staff.getRole().getName() : null);
        user.put("permissions", loadPermissions(staff));
        return new StaffLoginResponse(
                (String) tokens.get("access_token"),
                (String) tokens.get("refresh_token"),
                String.valueOf(tokens.getOrDefault("token_type", "Bearer")),
                num(tokens.get("expires_in")),
                num(tokens.get("refresh_expires_in")),
                user);
    }

    public TokenRefreshResponse refresh(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        Map<String, Object> tokens = requestTokens(form, "Session expired. Please log in again.");
        return new TokenRefreshResponse(
                (String) tokens.get("access_token"),
                (String) tokens.get("refresh_token"),
                String.valueOf(tokens.getOrDefault("token_type", "Bearer")),
                num(tokens.get("expires_in")),
                num(tokens.get("refresh_expires_in")));
    }

    public void logout(String refreshToken) {
        endSession(refreshToken);
    }

    private Map<String, Object> requestTokens(MultiValueMap<String, String> form, String defaultError) {
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        try {
            Map<String, Object> res = rest.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (res == null || res.get("access_token") == null) {
                throw new IllegalStateException("Keycloak returned no token");
            }
            return res;
        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            log.warn("Keycloak staff token request failed: {} {}", e.getStatusCode(), body);
            if (body.contains("not fully set up")) {
                throw new BadRequestException(
                        "Account setup is not finished. Use the link in your invitation email.");
            }
            if (body.contains("Account disabled")) {
                throw new BadRequestException("This account has been deactivated");
            }
            throw new BadRequestException(defaultError);
        }
    }

    private void endSession(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);
        try {
            rest.post()
                    .uri("/realms/{realm}/protocol/openid-connect/logout", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            log.debug("Keycloak logout failed: {}", e.getStatusCode());
        }
    }

    private static long num(Object o) {
        return o instanceof Number n ? n.longValue() : 0L;
    }


    private List<String> loadPermissions(User staff) {
        if (staff.getRole() == null) return List.of();
        return staff.getRole().getPermissions().stream()
                .map(Permission::getName)      // the key, e.g. "PRODUCT_READ"
                .distinct()
                .sorted()
                .toList();
    }
}