package com.coffee_shop.coffee_shop.config;

import com.coffee_shop.coffee_shop.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                // public — no auth required
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/orders/**").permitAll()
                                .requestMatchers("/api/addons/**").permitAll() // menu browsing, adjust as needed
                                .requestMatchers("/api/products/**").permitAll()
                                .requestMatchers("/api/payments/**").permitAll()
                                .requestMatchers(
                                        "/",
                                        "/index.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html",
                                        "/webjars/**"
                                ).permitAll()
//                                .requestMatchers(HttpMethod.GET, "/models/**").hasAuthority("SALE")
//                                .requestMatchers(HttpMethod.POST, "/brands").hasAuthority(BRAND_WRITE.getDescription())
//                                .requestMatchers(HttpMethod.GET, "/models/**").hasAuthority(MODEL_READ.getDescription())
//                                .requestMatchers(HttpMethod.GET, "/reports/**").hasAuthority(REPORT.getDescription())
//                                  .requestMatchers("/**").hasRole("ADMIN")
//                                .requestMatchers("/brands").hasRole("STOCKER")
//                                .requestMatchers("/reports/**").hasRole("SALE")
                                // everything else requires a valid JWT
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}