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

    // 🟣 JWT DECODER — Validate Keycloak tokens
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/myrealm}")
            String issuerUri) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(issuerUri + "/protocol/openid-connect/certs")
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuerUri)
        );

        return decoder;
    }

    // 🔵 CORS — Allow Angular frontends to call the API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 🟢 SECURITY FILTER CHAIN
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                // 🟡 REST API using Bearer tokens — stateless, no sessions
                .csrf(csrf -> csrf.disable())
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Authentication and authorization error handlers
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // ══════════════════════════════════════════════════════════════
                //  URL AUTHORIZATION RULES
                //  Order matters: Spring checks from TOP to BOTTOM and uses the
                //  FIRST matching rule. More specific patterns must come first.
                // ══════════════════════════════════════════════════════════════
                .authorizeHttpRequests(auth -> auth

                        // ─────────────────────────────────────────────────────
                        // 🌐  ALWAYS PUBLIC — no token needed
                        // ─────────────────────────────────────────────────────

                        // CORS preflight — browsers send OPTIONS before every cross-origin request
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger / OpenAPI UI
                        .requestMatchers(
                                "/", "/index.html",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-ui.html", "/webjars/**"
                        ).permitAll()

                        // ── Customer auth endpoints ─────────────────────────
                        // POST /api/auth/register        — new customer sign-up
                        // POST /api/auth/login           — get access + refresh token
                        // POST /api/auth/refresh         — exchange refresh → new access token
                        // POST /api/auth/logout          — revoke refresh token
                        // POST /api/auth/forgot-password — send reset-password email
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/auth/forgot-password"
                        ).permitAll()

                        // ── Staff auth endpoints ────────────────────────────
                        // POST /api/auth/staff/login    — staff login (OTP-aware)
                        // POST /api/auth/staff/refresh  — token refresh
                        // POST /api/auth/staff/logout   — revoke token
                        .requestMatchers("/api/auth/staff/**").permitAll()

                        // ── Public menu browsing (customers don't need to log in) ──
                        .requestMatchers(HttpMethod.GET,
                                "/api/products",
                                "/api/products/feature",
                                "/api/products/new",
                                "/api/products/*/category",
                                "/api/products/*"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/categories",
                                "/api/categories/*"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/addons",
                                "/api/addons/*"
                        ).permitAll()

                        // Public review reading — anyone can read product reviews & ratings
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/*/reviews",
                                "/api/products/*/reviews/summary"
                        ).permitAll()

                        // ─────────────────────────────────────────────────────
                        // 🛒  CUSTOMER — must be authenticated with CUSTOMER role
                        // ─────────────────────────────────────────────────────

                        // Customer profile — GET /api/customers/me, PUT /api/customers/me, PUT /api/customers/me/password
                        .requestMatchers("/api/customers/**").hasRole("CUSTOMER")

                        // Address management — customer manages their own delivery addresses
                        .requestMatchers("/api/addresses/**").hasRole("CUSTOMER")

                        // Customer places an order (fine-grained ownership enforced in controller via @PreAuthorize)
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")

                        // Customer views their own order / any authenticated user with ORDER_READ
                        .requestMatchers(HttpMethod.GET,
                                "/api/orders/customer/*",
                                "/api/orders/*"
                        ).authenticated()

                        // Customer or staff can cancel (ownsOrder guard in controller)
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/cancel").authenticated()

                        // Customer initiates KHQR payment for their own order
                        .requestMatchers(HttpMethod.POST, "/api/payments/khqr/*").authenticated()

                        // Customer checks status of their own payment
                        .requestMatchers(HttpMethod.GET, "/api/payments/*/status").authenticated()

                        // Customer writes / edits / deletes their own review
                        .requestMatchers(HttpMethod.POST,   "/api/reviews").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT,    "/api/reviews/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,    "/api/reviews/me").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET,    "/api/products/*/reviews/can-review").hasRole("CUSTOMER")

                        // /api/me — identity endpoint for both customer & staff
                        .requestMatchers("/api/me").authenticated()

                        // ─────────────────────────────────────────────────────
                        // 🔧  STAFF — fine-grained authority per resource
                        //  Both _MANAGE (broad) and _READ/_CREATE/_UPDATE/_DELETE
                        //  (granular) are accepted at the URL level.
                        //  Method-level @PreAuthorize in each controller provides
                        //  the second layer of defence.
                        // ─────────────────────────────────────────────────────

                        // ── Products ───────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/products/pagination")
                                .hasAnyAuthority("PRODUCT_READ", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/products/**")
                                .hasAnyAuthority("PRODUCT_CREATE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/products/**")
                                .hasAnyAuthority("PRODUCT_UPDATE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,  "/api/products/**")
                                .hasAnyAuthority("PRODUCT_UPDATE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                                .hasAnyAuthority("PRODUCT_DELETE", "PRODUCT_MANAGE")

                        // ── Categories ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/categories/**")
                                .hasAnyAuthority("CATEGORY_READ", "CATEGORY_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/categories/**")
                                .hasAnyAuthority("CATEGORY_CREATE", "CATEGORY_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/categories/**")
                                .hasAnyAuthority("CATEGORY_UPDATE", "CATEGORY_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,  "/api/categories/**")
                                .hasAnyAuthority("CATEGORY_UPDATE", "CATEGORY_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**")
                                .hasAnyAuthority("CATEGORY_DELETE", "CATEGORY_MANAGE")

                        // ── Addons ─────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/addons/**")
                                .hasAnyAuthority("ADDON_CREATE", "ADDON_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/addons/**")
                                .hasAnyAuthority("ADDON_UPDATE", "ADDON_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,  "/api/addons/**")
                                .hasAnyAuthority("ADDON_UPDATE", "ADDON_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/addons/**")
                                .hasAnyAuthority("ADDON_DELETE", "ADDON_MANAGE")

                        // ── Addon Ingredients ──────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/addons/*/ingredients/**")
                                .hasAnyAuthority("ADDON_INGREDIENT_READ", "ADDON_INGREDIENT_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/addons/*/ingredients/**")
                                .hasAnyAuthority("ADDON_INGREDIENT_CREATE", "ADDON_INGREDIENT_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/addons/*/ingredients/**")
                                .hasAnyAuthority("ADDON_INGREDIENT_UPDATE", "ADDON_INGREDIENT_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/addons/*/ingredients/**")
                                .hasAnyAuthority("ADDON_INGREDIENT_DELETE", "ADDON_INGREDIENT_MANAGE")

                        // ── Variants ───────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/variant/**")
                                .hasAnyAuthority("VARIANT_READ", "VARIANT_MANAGE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/variant/**")
                                .hasAnyAuthority("VARIANT_CREATE", "VARIANT_MANAGE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/variant/**")
                                .hasAnyAuthority("VARIANT_UPDATE", "VARIANT_MANAGE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,  "/api/variant/**")
                                .hasAnyAuthority("VARIANT_UPDATE", "VARIANT_MANAGE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/variant/**")
                                .hasAnyAuthority("VARIANT_DELETE", "VARIANT_MANAGE", "PRODUCT_MANAGE")

                        // ── Recipes ────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/recipes/**")
                                .hasAnyAuthority("RECIPE_READ", "RECIPE_MANAGE", "INVENTORY_MANAGE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/recipes/**")
                                .hasAnyAuthority("RECIPE_CREATE", "RECIPE_MANAGE", "INVENTORY_MANAGE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/recipes/**")
                                .hasAnyAuthority("RECIPE_UPDATE", "RECIPE_MANAGE", "INVENTORY_MANAGE", "PRODUCT_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/recipes/**")
                                .hasAnyAuthority("RECIPE_DELETE", "RECIPE_MANAGE", "INVENTORY_MANAGE", "PRODUCT_MANAGE")

                        // ── Ingredients ────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/ingredient/**")
                                .hasAnyAuthority("INGREDIENT_READ", "INGREDIENT_MANAGE")
                        // POST is also used for stock increment/decrement (treated as UPDATE)
                        .requestMatchers(HttpMethod.POST,   "/api/ingredient/**")
                                .hasAnyAuthority("INGREDIENT_CREATE", "INGREDIENT_UPDATE", "INGREDIENT_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/ingredient/**")
                                .hasAnyAuthority("INGREDIENT_UPDATE", "INGREDIENT_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,  "/api/ingredient/**")
                                .hasAnyAuthority("INGREDIENT_UPDATE", "INGREDIENT_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/ingredient/**")
                                .hasAnyAuthority("INGREDIENT_DELETE", "INGREDIENT_MANAGE")

                        // ── Suppliers ──────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/suppliers/**")
                                .hasAnyAuthority("SUPPLIER_READ", "SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/suppliers/**")
                                .hasAnyAuthority("SUPPLIER_CREATE", "SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/suppliers/**")
                                .hasAnyAuthority("SUPPLIER_UPDATE", "SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,  "/api/suppliers/**")
                                .hasAnyAuthority("SUPPLIER_UPDATE", "SUPPLIER_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**")
                                .hasAnyAuthority("SUPPLIER_DELETE", "SUPPLIER_MANAGE")

                        // ── Purchase Orders ────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/purchase-orders/**")
                                .hasAnyAuthority("PURCHASE_ORDER_READ", "PURCHASE_ORDER_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/purchase-orders/**")
                                .hasAnyAuthority("PURCHASE_ORDER_CREATE", "PURCHASE_ORDER_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/purchase-orders/**")
                                .hasAnyAuthority("PURCHASE_ORDER_UPDATE", "PURCHASE_ORDER_MANAGE")
                        // PATCH is used for order/receive/cancel status transitions
                        .requestMatchers(HttpMethod.PATCH,  "/api/purchase-orders/**")
                                .hasAnyAuthority("PURCHASE_ORDER_UPDATE", "PURCHASE_ORDER_DELETE", "PURCHASE_ORDER_MANAGE")

                        // ── Stock Adjustments (read-only via API) ──────────────
                        .requestMatchers("/api/stock-adjustment/**")
                                .hasAnyAuthority("STOCK_ADJUSTMENT_READ", "STOCK_ADJUSTMENT_MANAGE")

                        // ── Inventory Transactions (read-only audit trail) ──────
                        .requestMatchers("/api/transactions/**")
                                .hasAnyAuthority("TRANSACTION_READ", "TRANSACTION_MANAGE")

                        // ── Orders — staff pipeline management ─────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/orders",
                                "/api/orders/all"
                        ).hasAnyAuthority("ORDER_READ", "ORDER_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/orders/*/confirm",
                                "/api/orders/*/preparing",
                                "/api/orders/*/ready",
                                "/api/orders/*/delivering",
                                "/api/orders/*/complete"
                        ).hasAnyAuthority("ORDER_UPDATE", "ORDER_MANAGE")

                        // ── Payments — staff overview ──────────────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/payments",
                                "/api/payments/all"
                        ).hasAnyAuthority("PAYMENT_READ", "PAYMENT_MANAGE")

                        // ── Users / Staff management ───────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/users/**")
                                .hasAnyAuthority("USER_READ", "USER_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/users/**")
                                .hasAnyAuthority("USER_CREATE", "USER_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/users/**")
                                .hasAnyAuthority("USER_UPDATE", "USER_MANAGE")
                        .requestMatchers(HttpMethod.PATCH,  "/api/users/**")
                                .hasAnyAuthority("USER_UPDATE", "USER_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                                .hasAnyAuthority("USER_DELETE", "USER_MANAGE")

                        // ── Roles ──────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/roles/**")
                                .hasAnyAuthority("ROLE_READ", "ROLE_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/roles/**")
                                .hasAnyAuthority("ROLE_CREATE", "ROLE_UPDATE", "ROLE_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/roles/**")
                                .hasAnyAuthority("ROLE_DELETE", "ROLE_UPDATE", "ROLE_MANAGE")

                        // ── Permissions ────────────────────────────────────────
                        // ROLE_READ / ROLE_MANAGE may also GET permissions (role drawer needs the list)
                        .requestMatchers(HttpMethod.GET,    "/api/permissions/**")
                                .hasAnyAuthority("PERMISSION_READ", "PERMISSION_MANAGE", "ROLE_READ", "ROLE_MANAGE")
                        .requestMatchers(HttpMethod.POST,   "/api/permissions/**")
                                .hasAnyAuthority("PERMISSION_CREATE", "PERMISSION_MANAGE")
                        .requestMatchers(HttpMethod.PUT,    "/api/permissions/**")
                                .hasAnyAuthority("PERMISSION_UPDATE", "PERMISSION_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/api/permissions/**")
                                .hasAnyAuthority("PERMISSION_DELETE", "PERMISSION_MANAGE")

                        // ── Audit Logs ─────────────────────────────────────────
                        .requestMatchers("/api/audit-logs/**")
                                .hasAnyAuthority("AUDIT_LOG_READ", "AUDIT_LOG_VIEW", "AUDIT_LOG_MANAGE")

                        // ── Staff review moderation ────────────────────────────
                        .requestMatchers("/api/staff/reviews/**")
                                .hasAnyAuthority("REVIEW_READ", "REVIEW_MODERATE", "REVIEW_MANAGE")

                        // ─────────────────────────────────────────────────────
                        // 🔒  Catch-all — anything not matched above must be authenticated
                        // ─────────────────────────────────────────────────────
                        .anyRequest().authenticated()
                )
                // 🟣 KEYCLOAK JWT AUTHENTICATION
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt ->
                                jwt.jwtAuthenticationConverter(keycloakJwtConverter)
                        ).authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }
}
