package com.coffee_shop.coffee_shop.service;

public interface CustomerLoginAttemptService {
    void registerFailedAttempt(Long customerId);

    void resetAttempts(Long customerId);
}