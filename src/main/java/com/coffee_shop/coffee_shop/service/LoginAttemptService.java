package com.coffee_shop.coffee_shop.service;

public interface LoginAttemptService {

    void registerFailedAttempt(Long userId);
}
