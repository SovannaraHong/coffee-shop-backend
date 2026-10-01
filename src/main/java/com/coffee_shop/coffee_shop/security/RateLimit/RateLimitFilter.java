package com.coffee_shop.coffee_shop.security.RateLimit;

import com.coffee_shop.coffee_shop.util.DeviceFingerprintUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Map<String, Integer> LIMITED_PATHS = Map.of(
            "/api/auth/login", 10,
            "/api/auth/verify-otp", 10,
            "/api/staff-auth/login", 10,
            "/api/staff-auth/verify-otp", 10,
            "/api/staff-auth/refresh", 20
    );
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket(int capacity) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        Integer capacity = LIMITED_PATHS.get(path);

        if (capacity != null) {
            String ip = DeviceFingerprintUtil.extractIp(req);
            String key = path + "|" + ip;
            Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(capacity));

            if (!bucket.tryConsume(1)) {
                res.setStatus(429);
                res.setHeader("Retry-After", "60");
                res.setContentType("application/json");
                res.getWriter().write("{\"message\":\"Too many requests. Try again in a minute.\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
