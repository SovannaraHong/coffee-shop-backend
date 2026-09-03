package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.RefreshTokenRequest;
import com.coffee_shop.coffee_shop.dto.request.UserLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.VerifyOtpRequest;
import com.coffee_shop.coffee_shop.dto.response.StaffTokenResponse;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginRequest request,
                                                     HttpServletRequest httpServletRequest) {
        userService.login(request, httpServletRequest);
        return ResponseEntity.ok(Map.of("message", "OTP sent to your email. Please verify to complete login."));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BadRequestException("Missing or invalid Authorization header");
        }
        String token = header.substring(7);
        userService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAllDevices(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BadRequestException("Missing or invalid Authorization header");
        }
        String token = header.substring(7);
        userService.logoutAllDevices(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<StaffTokenResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(userService.verifyLoginOtp(request, httpServletRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<StaffTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(userService.refresh(request, httpServletRequest));
    }
}