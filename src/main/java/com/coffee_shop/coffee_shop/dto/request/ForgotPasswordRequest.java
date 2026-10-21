package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ADDED (2026-10-08): Request body for POST /api/auth/forgot-password.
 * Public endpoint — customer submits only their email.
 * Keycloak sends a reset-password link to that email (valid 1 day = 86400s).
 * No password is sent here — the customer sets the new one by clicking the email link.
 */
@Data
public class ForgotPasswordRequest {

    // The email the customer registered with — used to find their Keycloak account
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
