package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.CustomerLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerRegisterRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerUpdateRequest;
import com.coffee_shop.coffee_shop.dto.request.VerifyOtpRequest;
import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;
import com.coffee_shop.coffee_shop.dto.response.LoginResponse;

public interface CustomerService {
    CustomerResponse register(CustomerRegisterRequest request);

    void verifyOtp(VerifyOtpRequest request);

    LoginResponse login(CustomerLoginRequest request);

    CustomerResponse getProfile(Long id);

    CustomerResponse updateProfile(Long id, CustomerUpdateRequest request);
}