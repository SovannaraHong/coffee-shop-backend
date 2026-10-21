package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String otp          // optional: 6-digit authenticator code
) {
}