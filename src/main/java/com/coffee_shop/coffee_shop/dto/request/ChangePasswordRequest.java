package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ADDED (2026-10-08): Request body for PUT /api/customers/me/password.
 * Authenticated endpoint — logged-in customer changes their own password.
 * Requires the current password to prevent someone from changing it
 * if they just grabbed an unlocked phone.
 */
@Data
public class ChangePasswordRequest {

    // Customer must confirm they know the current password before changing it
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    // The new password — Keycloak hashes it, never stored in our DB
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String newPassword;
}
