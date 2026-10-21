package com.coffee_shop.coffee_shop.security.keycloak;

import com.coffee_shop.coffee_shop.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KeyCloakAdminService {
    private final RestClient restClient;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    // ADDED (2026-10-08): public/frontend client used for verifyPassword() via password grant
    private final String userClientId;

    private String cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public KeyCloakAdminService(@Value("${keycloak.base-url}") String baseUrl,
                                @Value("${keycloak.realm}") String realm,
                                @Value("${keycloak.admin-client-id}") String clientId,
                                @Value("${keycloak.admin-client-secret}") String clientSecret,
                                // ADDED (2026-10-08): injected for verifyPassword() — defaults to admin-web
                                @Value("${keycloak.user-client-id:admin-web}") String userClientId) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.userClientId = userClientId;
    }

    private synchronized String token() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<String, Object> res = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        if (res == null || res.get("access_token") == null) {
            throw new IllegalStateException("Could not obtain Keycloak admin token");
        }
        cachedToken = (String) res.get("access_token");
        long expiresIn = ((Number) res.getOrDefault("expires_in", 60)).longValue();
        tokenExpiry = Instant.now().plusSeconds(Math.max(expiresIn - 30, 5));
        return cachedToken;
    }

    public String createUser(String email, String firstName, String lastName, List<String> requiredActions) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("username", email);
        user.put("email", email);
        if (firstName != null && !firstName.isBlank()) user.put("firstName", firstName);
        if (lastName != null && !lastName.isBlank()) user.put("lastName", lastName);
        user.put("enabled", true);

        URI location;
        try {
            location = restClient.post()
                    .uri("/admin/realms/{realm}/users", realm)
                    .headers(h -> h.setBearerAuth(token()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders().getLocation();
        } catch (HttpClientErrorException.Conflict e) {
            throw new BadRequestException("Email is already registered");
        }

        if (location == null) {
            throw new IllegalStateException("Keycloak did not return the new user location");
        }
        String path = location.getPath();
        String id = path.substring(path.lastIndexOf('/') + 1);

        if (requiredActions != null && !requiredActions.isEmpty()) {
            try {
                sendActionsEmail(id, requiredActions);
            } catch (RuntimeException e) {
                deleteUser(id);   // don't leave a user nobody was invited to
                throw e;
            }
        }
        return id;
    }

    /**
     * True if the user has an authenticator-app (TOTP) credential.
     */
    public boolean hasOtp(String email) {
        List<Map<String, Object>> users = restClient.get()
                .uri(b -> b.path("/admin/realms/{realm}/users")
                        .queryParam("email", email)
                        .queryParam("exact", true)
                        .build(realm))
                .headers(h -> h.setBearerAuth(token()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
        if (users == null || users.isEmpty()) return false;

        String userId = (String) users.get(0).get("id");
        List<Map<String, Object>> creds = restClient.get()
                .uri("/admin/realms/{realm}/users/{id}/credentials", realm, userId)
                .headers(h -> h.setBearerAuth(token()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
        return creds != null && creds.stream().anyMatch(c -> "otp".equals(c.get("type")));
    }

    /**
     * Registers a customer in Keycloak with a pre-set password.
     * <p>
     * ADDED (2026-10-08): Used by the customer self-registration flow (POST /api/auth/register).
     * Key difference from createUser() (staff flow):
     * - Staff:    no password set → Keycloak sends UPDATE_PASSWORD + VERIFY_EMAIL + CONFIGURE_TOTP
     * - Customer: password set here → Keycloak sends VERIFY_EMAIL only (simpler, one click)
     *
     * @param email     used as both Keycloak username and email
     * @param firstName stored in Keycloak profile
     * @param lastName  stored in Keycloak profile
     * @param password  customer's chosen password (plain text — Keycloak hashes it, never stored in our DB)
     * @return Keycloak user ID (UUID) of the newly created customer
     */
    public String registerCustomer(String email, String firstName, String lastName, String password) {
        // Build the Keycloak user payload with password credentials already set
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("username", email);
        user.put("email", email);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("enabled", true);
        user.put("emailVerified", false); // false until customer clicks the verify link

        // Set customer's chosen password directly — Keycloak hashes it internally
        // "temporary: false" means the customer is NOT forced to change it on first login
        user.put("credentials", List.of(Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        )));

        // Only VERIFY_EMAIL required — password is already set above
        // Staff flow uses: ["VERIFY_EMAIL", "UPDATE_PASSWORD", "CONFIGURE_TOTP"]
        user.put("requiredActions", List.of("VERIFY_EMAIL"));

        URI location;
        try {
            location = restClient.post()
                    .uri("/admin/realms/{realm}/users", realm)
                    .headers(h -> h.setBearerAuth(token()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders().getLocation();
        } catch (HttpClientErrorException.Conflict e) {
            // Email is already taken in Keycloak
            throw new BadRequestException("Email is already registered");
        }

        if (location == null) {
            throw new IllegalStateException("Keycloak did not return the new user location");
        }
        String path = location.getPath();
        String id = path.substring(path.lastIndexOf('/') + 1);

        // Send verification email — customer must click to activate the account
        try {
            sendActionsEmail(id, List.of("VERIFY_EMAIL"));
        } catch (RuntimeException e) {
            deleteUser(id); // rollback: don't leave an orphan Keycloak account
            throw e;
        }
        return id;
    }


    public void sendActionsEmail(String userId, List<String> actions) {
        try {
            restClient.put()
                    .uri(b -> b.path("/admin/realms/{realm}/users/{id}/execute-actions-email")
                            .queryParam("lifespan", 86400) // 86400 seconds = 1 day (24 hours)
                            .build(realm, userId))
                    .headers(h -> h.setBearerAuth(token()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(actions)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpServerErrorException e) {
            // Keycloak returns 500 when SMTP is not configured in Realm Settings → Email.
            // Without SMTP, it cannot send the verification/password-reset email.
            throw new IllegalStateException(
                    "Keycloak could not send the verification email. " +
                            "Please configure SMTP in Keycloak Admin Console → Realm Settings → Email " +
                            "(Host: smtp.gmail.com, Port: 587, From: your-email@gmail.com, " +
                            "Username + App-Password). Raw Keycloak error: " + e.getMessage(), e);
        }
    }

    public void changeEmail(String userId, String newEmail) {
        try {
            patchUser(userId, Map.of("email", newEmail, "username", newEmail, "emailVerified", false));
        } catch (HttpClientErrorException.Conflict e) {
            throw new BadRequestException("Email is already registered");
        }
        sendActionsEmail(userId, List.of("VERIFY_EMAIL"));
    }

    private void patchUser(String userId, Map<String, Object> changes) {
        Map<String, Object> current = restClient.get()
                .uri("/admin/realms/{realm}/users/{id}", realm, userId)
                .headers(h -> h.setBearerAuth(token()))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        if (current == null) {
            throw new IllegalStateException("Keycloak user not found: " + userId);
        }
        current.putAll(changes);
        restClient.put()
                .uri("/admin/realms/{realm}/users/{id}", realm, userId)
                .headers(h -> h.setBearerAuth(token()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(current)
                .retrieve()
                .toBodilessEntity();
    }

    public void setEnabled(String userId, boolean enabled) {
        patchUser(userId, Map.of("enabled", enabled));
        if (!enabled) {
            logout(userId);   // also end any active sessions
        }
    }

    public void clearBruteForce(String userId) {
        restClient.delete()
                .uri("/admin/realms/{realm}/attack-detection/brute-force/users/{id}", realm, userId)
                .headers(h -> h.setBearerAuth(token()))
                .retrieve()
                .toBodilessEntity();
    }

    public void logout(String userId) {
        restClient.post()
                .uri("/admin/realms/{realm}/users/{id}/logout", realm, userId)
                .headers(h -> h.setBearerAuth(token()))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteUser(String userId) {
        restClient.delete()
                .uri("/admin/realms/{realm}/users/{id}", realm, userId)
                .headers(h -> h.setBearerAuth(token()))
                .retrieve()
                .toBodilessEntity();
    }


    public void sendForgotPasswordEmail(String email) {
        // Search Keycloak for users with this exact email
        List<Map<String, Object>> users = restClient.get()
                .uri(b -> b.path("/admin/realms/{realm}/users")
                        .queryParam("email", email)
                        .queryParam("exact", true) // exact match only — not partial
                        .build(realm))
                .headers(h -> h.setBearerAuth(token()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

        // If no user found — silently return (don't reveal whether email exists)
        if (users == null || users.isEmpty()) return;

        String userId = (String) users.get(0).get("id");

        // Send UPDATE_PASSWORD action email — link valid for 1 day (86400 seconds)
        sendActionsEmail(userId, List.of("UPDATE_PASSWORD"));
    }


    public void resetPassword(String keycloakId, String newPassword) {
        // Keycloak's reset-password endpoint replaces the user's credentials
        restClient.put()
                .uri("/admin/realms/{realm}/users/{id}/reset-password", realm, keycloakId)
                .headers(h -> h.setBearerAuth(token()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "type", "password",
                        "value", newPassword,   // Keycloak hashes this — never stored as plain text
                        "temporary", false       // false = no forced change on next login
                ))
                .retrieve()
                .toBodilessEntity();
    }

  
    public boolean verifyPassword(String email, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", userClientId); // must have Direct Access Grants ON
        form.add("username", email);
        form.add("password", password);

        try {
            Map<String, Object> res = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            // If we got an access_token back → password is correct
            return res != null && res.get("access_token") != null;
        } catch (HttpClientErrorException.Unauthorized e) {
            // 401 from Keycloak = wrong password
            return false;
        }
    }
}