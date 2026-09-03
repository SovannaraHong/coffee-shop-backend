package com.coffee_shop.coffee_shop.service;

public interface IpLoginAttemptService {
    void checkNotBanned(String ip);

    void registerFailedAttempt(String ip);

    void resetAttempts(String ip);
}