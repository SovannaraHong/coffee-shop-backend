package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.ChangePasswordRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;
import com.coffee_shop.coffee_shop.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * All endpoints here require a valid CUSTOMER JWT token.
 * The @PreAuthorize("hasRole('CUSTOMER')") at class level enforces this.
 *
 * authentication.getPrincipal() returns the DB customer ID (Long) set by KeycloakJwtConverter.
 * For change-password we also need the Keycloak UUID (JWT "sub" claim) to call Keycloak API.
 */
@RequestMapping("api/customers")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final CustomerService customerService;

    // GET /api/customers/me — returns the logged-in customer's profile
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(customerService.getProfile(customerId));
    }

    // PUT /api/customers/me — update name and phone
    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(Authentication authentication,
                                                            @Valid @RequestBody CustomerUpdateRequest request) {
        Long customerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(customerService.updateProfile(customerId, request));
    }

    /**
     * ADDED (2026-10-08): PUT /api/customers/me/password
     * Logged-in customer changes their own password.
     *
     * Request body: { "currentPassword": "old123", "newPassword": "new456789" }
     *
     * Flow:
     *   1. Extract Keycloak UUID from JWT "sub" claim (needed to call Keycloak API).
     *   2. Verify currentPassword is correct via Keycloak token endpoint.
     *   3. If wrong → 400 Bad Request "Current password is incorrect".
     *   4. If correct → update password in Keycloak via reset-password API.
     *
     * Prerequisite: Keycloak client (admin-web) must have "Direct Access Grants" enabled.
     * Keycloak → Clients → admin-web → Settings → Direct Access Grants → ON
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        // Get the Keycloak UUID (sub claim) from the JWT — needed for Keycloak API calls
        // authentication.getCredentials() returns the raw Jwt object
        Jwt jwt = (Jwt) authentication.getCredentials();
        String keycloakId = jwt.getSubject(); // the "sub" claim = Keycloak user UUID
        customerService.changePassword(keycloakId, request);
        return ResponseEntity.ok().build();
    }
}