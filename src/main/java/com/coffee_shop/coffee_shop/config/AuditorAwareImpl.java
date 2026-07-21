package com.coffee_shop.coffee_shop.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.empty();
    }

//    @Override
//    public Optional<String> getCurrentAuditor() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()
//                || "anonymousUser".equals(authentication.getPrincipal())) {
//            return Optional.of("SYSTEM");
//        }
//
//        return Optional.of(authentication.getName());
//    }
}