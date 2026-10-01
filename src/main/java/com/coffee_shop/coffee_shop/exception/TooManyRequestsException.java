package com.coffee_shop.coffee_shop.exception;

import lombok.Getter;

@Getter
public class TooManyRequestsException extends RuntimeException {

    public static final String IP_BANNED = "IP_BANNED";
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

    private final String code;
    private final long retryAfterSeconds;

    public TooManyRequestsException(String code, String message, long retryAfterSeconds) {
        super(message);
        this.code = code;
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }
}