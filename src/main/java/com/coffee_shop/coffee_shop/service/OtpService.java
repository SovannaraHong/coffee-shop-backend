package com.coffee_shop.coffee_shop.service;

public interface OtpService {
    void generateAndSendOtp(String email);

    void verifyOtp(String email, String code);
}
