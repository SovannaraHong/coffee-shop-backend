package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.CustomerLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerRegisterRequest;
import com.coffee_shop.coffee_shop.dto.request.ForgotPasswordRequest;
import com.coffee_shop.coffee_shop.dto.request.RefreshTokenRequest;
import com.coffee_shop.coffee_shop.dto.response.CustomerLoginResponse;
import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;
import com.coffee_shop.coffee_shop.dto.response.TokenRefreshResponse;
import com.coffee_shop.coffee_shop.security.keycloak.CustomerAuthService;
import com.coffee_shop.coffee_shop.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;
    private final CustomerAuthService customerAuthService;

    // Customer self-registration: creates the user in Keycloak, sends the verify-email link, saves the profile.
    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(request));
    }

    // Password reset link by email. Always 200 so attackers can't probe which emails exist.
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        customerService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerLoginResponse> login(@Valid @RequestBody CustomerLoginRequest request) {
        return ResponseEntity.ok(customerAuthService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(customerAuthService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        customerAuthService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}