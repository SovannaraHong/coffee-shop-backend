package com.coffee_shop.coffee_shop.util;

public final class LockoutPolicy {

    public static final int MAX_FAILED_ATTEMPTS = 3; // strikes before a lock kicks in

    // stage 0 -> 1 min, stage 1 -> 5 min, stage 2 -> 1 hour, stage 3+ -> 24 hours (capped)
    private static final long[] LOCKOUT_MINUTES = {1, 5, 60, 24 * 60};

    private LockoutPolicy() {
    }

    public static long minutesForStage(int stage) {
        int idx = Math.min(stage, LOCKOUT_MINUTES.length - 1);
        return LOCKOUT_MINUTES[idx];
    }

    public static int nextStage(int stage) {
        return Math.min(stage + 1, LOCKOUT_MINUTES.length - 1);
    }
}