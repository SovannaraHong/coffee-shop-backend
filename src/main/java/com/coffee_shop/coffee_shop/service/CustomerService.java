package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.ChangePasswordRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerRegisterRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerUpdateRequest;
import com.coffee_shop.coffee_shop.dto.request.ForgotPasswordRequest;
import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;

public interface CustomerService {

    /**
     * ADDED (2026-10-08): Self-registration for customers.
     * Creates the customer in Keycloak (with password) and in our local DB,
     * then triggers a VERIFY_EMAIL action email.
     */
    CustomerResponse register(CustomerRegisterRequest request);

    /**
     * ADDED (2026-10-08): Forgot-password — sends reset link to customer's email.
     * Public — customer is NOT logged in when calling this.
     * Silently does nothing if email not found (prevents email enumeration attacks).
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * ADDED (2026-10-08): Change-password — logged-in customer changes their own password.
     * Verifies the currentPassword first via Keycloak token endpoint before resetting.
     *
     * @param keycloakId the logged-in customer's Keycloak UUID (JWT "sub" claim)
     */
    void changePassword(String keycloakId, ChangePasswordRequest request);

    // Returns the customer's own profile — used by GET /api/customers/me
    CustomerResponse getProfile(Long id);

    // Updates the customer's own profile — used by PUT /api/customers/me
    CustomerResponse updateProfile(Long id, CustomerUpdateRequest request);
}