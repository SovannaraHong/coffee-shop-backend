package com.coffee_shop.coffee_shop.config;


import com.coffee_shop.coffee_shop.security.jwt.JwtAuthenticationFilter;
import com.coffee_shop.coffee_shop.security.jwt.RestAccessDeniedHandler;
import com.coffee_shop.coffee_shop.security.jwt.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService; // CustomUserDetailsService, for STAFF login only
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final PasswordEncoder passwordEncoder;


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // --- fully public ---
                        .requestMatchers("/api/auth/**").permitAll()          // customer register/login/otp
                        .requestMatchers("/api/staff-auth/**").permitAll()    // staff login/otp/refresh
                        .requestMatchers("/api/products/**").permitAll()      // menu browsing
                        .requestMatchers("/api/addons/**").permitAll()        // menu browsing
                        .requestMatchers(
                                "/", "/index.html",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**"
                        ).permitAll()

                        // NOTE: /api/orders/** and /api/payments/** were previously permitAll —
                        // that means ANYONE could create orders/payments for ANY customer without
                        // logging in. Recommend requiring authentication here instead:
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()

                        // --- everything else requires a valid token ---
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CommandLineRunner generatePassword(PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("=================================");
            System.out.println(passwordEncoder.encode("Admin@123"));
            System.out.println("=================================");
        };
    }
}