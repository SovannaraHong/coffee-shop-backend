package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.CustomerLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerRegisterRequest;
import com.coffee_shop.coffee_shop.dto.request.VerifyOtpRequest;
import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;
import com.coffee_shop.coffee_shop.dto.response.LoginResponse;
import com.coffee_shop.coffee_shop.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        customerService.verifyOtp(request);
        return ResponseEntity.ok(Map.of("message", "Account verified. You can now log in."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody CustomerLoginRequest request) {
        return ResponseEntity.ok(customerService.login(request));
    }
}