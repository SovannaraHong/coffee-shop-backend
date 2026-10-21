package com.coffee_shop.coffee_shop.security.keycloak;

import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.repository.CustomerRepository;
import com.coffee_shop.coffee_shop.util.enums.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerProvisioningService {
    private final CustomerRepository customerRepository;

    public Customer getOrCreateCustomer(Jwt jwt) {
        String sub = jwt.getSubject();

        return customerRepository.findByKeycloakId(sub)
                .orElseGet(() -> {
                    try {
                        return customerRepository.saveAndFlush(linkOrCreate(jwt));
                    } catch (DataIntegrityViolationException e) {
                        return customerRepository.findByKeycloakId(sub)
                                .orElseThrow(() -> e);
                    }
                });
    }

    //    helper method
    private Customer linkOrCreate(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new BadCredentialsException("Token has no email claim");
        }
        boolean verified = Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"));
        Optional<Customer> existing = customerRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            Customer c = existing.get();
            if (c.getKeycloakId() != null && !verified) {
                throw new BadCredentialsException("Email belongs to another account or is not verified");
            }
            c.setKeycloakId(jwt.getSubject());
            c.setIsVerified(true);
            return c;
        }
        String first = firstNonBlank(jwt.getClaimAsString("given_name"), email.split("@")[0]);
        String last = firstNonBlank(jwt.getClaimAsString("family_name"), "-");

        return Customer.builder()
                .keycloakId(jwt.getSubject())
                .email(email)
                .firstName(first)
                .lastName(last)
                .authProvider(AuthProvider.LOCAL)
                .isVerified(verified)
                .isActive(true)
                .build();

    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private static String truncate(String s) {
        return s.length() > 150 ? s.substring(0, 150) : s;
    }
}
