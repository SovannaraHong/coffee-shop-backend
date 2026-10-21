package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.security.keycloak.StaffAuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/staff")
@RequiredArgsConstructor
public class StaffAuthController {

    public record StaffLoginRequest(@NotBlank @Email String email, @NotBlank String password, String otp) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    private final StaffAuthService staffAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody StaffLoginRequest req) {
        if (staffAuthService.otpRequired(req.email(), req.otp())) {
            return ResponseEntity.ok(Map.of("otpRequired", true));   // UI shows the code screen
        }
        return ResponseEntity.ok(staffAuthService.login(req.email(), req.password(), req.otp()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(staffAuthService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest req) {
        staffAuthService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }
}