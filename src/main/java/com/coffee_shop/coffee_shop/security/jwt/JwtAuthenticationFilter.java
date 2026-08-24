package com.coffee_shop.coffee_shop.security.jwt;

import com.coffee_shop.coffee_shop.repository.UserSessionRepository;
import com.coffee_shop.coffee_shop.service.JwtService;
import com.coffee_shop.coffee_shop.util.DeviceFingerprintUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.isTokenValid(token)) {
                String type = jwtService.extractClaims(token).get("type", String.class);

                if ("STAFF_ACCESS".equals(type)) {
                    authenticateStaff(token, request);

                } else if ("CUSTOMER".equals(type)) {
                    authenticateCustomer(token);
                }
                // STAFF_REFRESH tokens are intentionally rejected here — only valid at /refresh
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateStaff(String token, HttpServletRequest request) {
        String sessionId = jwtService.extractSessionId(token);
        String tokenDevice = jwtService.extractDeviceFingerprint(token);

        String currentFingerprint = DeviceFingerprintUtil.fingerprint(
                DeviceFingerprintUtil.extractDeviceInfo(request),
                DeviceFingerprintUtil.extractIp(request)
        );

        userSessionRepository.findBySessionIdAndRevokedFalse(sessionId)
                .filter(session -> currentFingerprint.equals(tokenDevice))
                .ifPresent(session -> {
                    session.setLastUsedAt(LocalDateTime.now());
                    userSessionRepository.save(session);

                    String email = jwtService.extractUsername(token);
                    List<GrantedAuthority> authorities = jwtService.extractAuthorities(token).stream()
                            .map(a -> (GrantedAuthority) new SimpleGrantedAuthority(a))
                            .toList();

                    var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
    }

    private void authenticateCustomer(String token) {
        Long customerId = jwtService.extractCustomerId(token);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

        var authToken = new UsernamePasswordAuthenticationToken(customerId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}