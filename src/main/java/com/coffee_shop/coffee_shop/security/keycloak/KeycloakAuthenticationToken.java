package com.coffee_shop.coffee_shop.security.keycloak;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

@Getter
public class KeycloakAuthenticationToken extends AbstractAuthenticationToken {

    private final Jwt jwt;
    private final Object principal;
    private final Long staffId;
    private final Long customerId;

    public KeycloakAuthenticationToken(
            Jwt jwt,
            Object principal,
            Long staffId,
            Long customerId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.jwt = jwt;
        this.principal = principal;
        this.staffId = staffId;
        this.customerId = customerId;

        super.setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public boolean isStaff() {
        return staffId != null;
    }
}
