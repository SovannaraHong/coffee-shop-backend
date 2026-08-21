package com.coffee_shop.coffee_shop.security.jwt;

import com.coffee_shop.coffee_shop.service.JwtService;
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
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.isTokenValid(token)) {
                String type = jwtService.extractClaims(token).get("type", String.class);

                if ("STAFF_ACCESS".equals(type)) {
                    String email = jwtService.extractUsername(token);
                    List<GrantedAuthority> authorities = jwtService.extractAuthorities(token).stream()
                            .map(a -> (GrantedAuthority) new SimpleGrantedAuthority(a))
                            .toList();

                    var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                } else if ("CUSTOMER".equals(type)) {
                    Long customerId = jwtService.extractCustomerId(token);
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

                    var authToken = new UsernamePasswordAuthenticationToken(customerId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                // STAFF_REFRESH tokens are intentionally rejected here — only valid at /refresh
            }
        }

        filterChain.doFilter(request, response);
    }
}