package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class CustomerRegisterRequest {

    // Customer's first name — stored in both Keycloak and local DB
    @NotBlank(message = "First name is required")
    @Size(max = 150)
    private String firstName;

    // Customer's last name — stored in both Keycloak and local DB
    @NotBlank(message = "Last name is required")
    @Size(max = 150)
    private String lastName;

    // Used as Keycloak username + email. Must be unique across all customers.
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    // Plain-text password — Keycloak hashes it. Never stored in our DB.
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    // Optional — customer can add/update phone later via PUT /api/customers/me
    // Pattern allows: +855 012345678, 012345678, +1234567890 etc.
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{6,20}$", message = "Invalid phone number format")
    private String phone;
}
