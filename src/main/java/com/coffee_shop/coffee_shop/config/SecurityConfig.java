package com.coffee_shop.coffee_shop.config;

import com.coffee_shop.coffee_shop.security.jwt.RestAccessDeniedHandler;
import com.coffee_shop.coffee_shop.security.jwt.RestAuthenticationEntryPoint;
import com.coffee_shop.coffee_shop.security.keycloak.KeycloakJwtConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final KeycloakJwtConverter keycloakJwtConverter;

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://localhost:4201}")
    private List<String> allowedOrigins;

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/myrealm}") String issuerUri) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(issuerUri + "/protocol/openid-connect/certs")
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-ui.html", "/webjars/**").permitAll()
                        .requestMatchers("/api/auth/staff/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh",
                                "/api/auth/logout", "/api/auth/forgot-password").permitAll()

                        // ---- public customer self-registration (ADDED 2026-10-08) ----
                        // No token needed — customer has no account yet when they hit this endpoint
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()

                        // ---- public forgot-password (ADDED 2026-10-08) ----
                        // No token needed — customer is not logged in when they forgot their password
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()

                        // ---- public menu browsing (reads only) ----
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/addons/**")
                        .permitAll()

                        // ---- menu management ----
                        .requestMatchers(HttpMethod.GET, "/api/variant/**").authenticated()
                        .requestMatchers("/api/addons/**", "/api/categories/**", "/api/variant/**")
                        .hasAuthority("PRODUCT_MANAGE")

                        // ---- inventory (staff only) ----
                        .requestMatchers("/api/ingredient/**", "/api/suppliers/**", "/api/purchase-orders/**",
                                "/api/stock-adjustment/**", "/api/transactions/**")
                        .hasAuthority("INVENTORY_MANAGE")

                        // ---- orders: staff-only actions ----
                        .requestMatchers(HttpMethod.GET, "/api/orders", "/api/orders/all").hasAuthority("ORDER_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/orders/*/confirm", "/api/orders/*/preparing", "/api/orders/*/ready",
                                "/api/orders/*/delivering", "/api/orders/*/complete")
                        .hasAuthority("ORDER_MANAGE")
                        .requestMatchers("/api/orders/**", "/api/payments/**").authenticated()

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }
}