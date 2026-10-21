package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.security.keycloak.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/me")
public class MeController {

    public record MeResponse(String keycloakId, Long staffId, Long customerId, String email,
                             String name, boolean staff, List<String> authorities) {
    }

    @GetMapping
    public MeResponse me(Authentication authentication) {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
        return new MeResponse(
                token.getJwt().getSubject(),
                token.getStaffId(),
                token.getCustomerId(),
                token.getJwt().getClaimAsString("email"),
                token.getJwt().getClaimAsString("name"),
                token.isStaff(),
                token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}