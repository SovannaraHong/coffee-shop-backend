package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.RefreshTokenRequest;
import com.coffee_shop.coffee_shop.dto.request.UserLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.VerifyOtpRequest;
import com.coffee_shop.coffee_shop.dto.response.StaffTokenResponse;
import com.coffee_shop.coffee_shop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("api/staff-auth")
@RestController
@RequiredArgsConstructor
public class StaffAuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginRequest request) {
        userService.login(request);
        return ResponseEntity.ok(Map.of("message", "OTP sent to your email. Please verify to complete login."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<StaffTokenResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(userService.verifyLoginOtp(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<StaffTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userService.refresh(request));
    }
}