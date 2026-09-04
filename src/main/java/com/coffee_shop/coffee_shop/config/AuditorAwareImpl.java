package com.coffee_shop.coffee_shop.config;

import com.coffee_shop.coffee_shop.entity.User;
import com.coffee_shop.coffee_shop.repository.UserRepository;
import com.coffee_shop.coffee_shop.security.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<User> {

    private final UserRepository userRepository;

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof AuthUser authUser) {
            return userRepository.findById(authUser.getId());
        }

        return Optional.empty();
    }
}