package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.RefreshTokenRequest;
import com.coffee_shop.coffee_shop.dto.request.UserCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.UserLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.VerifyOtpRequest;
import com.coffee_shop.coffee_shop.dto.response.StaffTokenResponse;
import com.coffee_shop.coffee_shop.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createStaff(UserCreateRequest request);

    // Step 1: verify credentials via AuthenticationManager, then send OTP. No token yet.
    void login(UserLoginRequest request);

    // Step 2: verify OTP, issue access + refresh tokens.
    StaffTokenResponse verifyLoginOtp(VerifyOtpRequest request);

    // Exchange a valid refresh token for a new access token.
    StaffTokenResponse refresh(RefreshTokenRequest request);

    List<UserResponse> getAll();

    UserResponse changeStatus(Long id);
}