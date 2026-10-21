package com.coffee_shop.coffee_shop.security.keycloak;

import com.coffee_shop.coffee_shop.dto.response.CustomerLoginResponse;
import com.coffee_shop.coffee_shop.dto.response.TokenRefreshResponse;
import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.mapper.CustomerMapper;
import com.coffee_shop.coffee_shop.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Customer login through Keycloak. Browsers never talk to Keycloak's token endpoint directly.
 */
@Slf4j
@Service
public class CustomerAuthService {

    private final RestClient rest;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final JwtDecoder jwtDecoder;
    private final CustomerProvisioningService provisioningService;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    public CustomerAuthService(@Value("${keycloak.base-url}") String baseUrl,
                               @Value("${keycloak.realm}") String realm,
                               @Value("${keycloak.customer-login-client-id}") String clientId,
                               @Value("${keycloak.customer-login-client-secret}") String clientSecret,
                               JwtDecoder jwtDecoder,
                               CustomerProvisioningService provisioningService,
                               UserRepository userRepository,
                               CustomerMapper customerMapper) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.jwtDecoder = jwtDecoder;
        this.provisioningService = provisioningService;
        this.userRepository = userRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerLoginResponse login(String email, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", email);
        form.add("password", password);
        Map<String, Object> tokens = requestTokens(form, "Invalid email or password");

        Jwt jwt = jwtDecoder.decode((String) tokens.get("access_token"));

        // staff must never get a token from the customer login
        if (userRepository.findByKeycloakId(jwt.getSubject()).isPresent()) {
            endSession((String) tokens.get("refresh_token"));
            throw new BadRequestException("Invalid email or password");
        }

        Customer customer;
        try {
            customer = provisioningService.getOrCreateCustomer(jwt);
        } catch (AuthenticationException e) {
            endSession((String) tokens.get("refresh_token"));
            throw new BadRequestException(e.getMessage());
        }
        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            endSession((String) tokens.get("refresh_token"));
            throw new BadRequestException("This account has been deactivated");
        }

        return new CustomerLoginResponse(
                (String) tokens.get("access_token"),
                (String) tokens.get("refresh_token"),
                String.valueOf(tokens.getOrDefault("token_type", "Bearer")),
                num(tokens.get("expires_in")),
                num(tokens.get("refresh_expires_in")),
                customerMapper.toResponse(customer));
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
            log.warn("Keycloak token request failed: {} {}", e.getStatusCode(), body);
            if (body.contains("not fully set up")) {
                throw new BadRequestException("Please verify your email before logging in.");
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
}