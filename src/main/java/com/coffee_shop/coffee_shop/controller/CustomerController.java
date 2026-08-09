package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.CustomerUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;
import com.coffee_shop.coffee_shop.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/customers")
@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(customerService.getProfile(customerId));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(Authentication authentication,
                                                            @Valid @RequestBody CustomerUpdateRequest request) {
        Long customerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(customerService.updateProfile(customerId, request));
    }
}