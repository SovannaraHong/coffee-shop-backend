package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.*;
import com.coffee_shop.coffee_shop.dto.response.StaffTokenResponse;
import com.coffee_shop.coffee_shop.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResponse createStaff(UserCreateRequest request);

    UserResponse updateStaff(Long id, UserUpdateRequest request);

    void delete(Long id);

    List<UserResponse> findAll();

    PageDTO<UserResponse> getPagination(Map<String, String> params);

    UserResponse uploadImage(Long id, MultipartFile file) throws Exception;


    // Step 1: verify credentials via AuthenticationManager, then send OTP. No token yet.
    void login(UserLoginRequest request, HttpServletRequest httpServletRequest);

    void logout(String token);

    void logoutAllDevices(String accessToken);

    // Step 2: verify OTP, issue access + refresh tokens.
    StaffTokenResponse verifyLoginOtp(VerifyOtpRequest request, HttpServletRequest httpServletRequest);

    // Exchange a valid refresh token for a new access token.
    StaffTokenResponse refresh(RefreshTokenRequest request, HttpServletRequest httpServletRequest);

    List<UserResponse> getAll();

    UserResponse changeStatus(Long id);

    UserResponse unlockAccount(Long id);
}