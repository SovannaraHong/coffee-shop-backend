package com.coffee_shop.coffee_shop.security.keycloak;

import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.entity.User;
import com.coffee_shop.coffee_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserRepository userRepository;
    private final CustomerProvisioningService customerProvisioningService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Optional<User> staff = userRepository.findByKeycloakId(jwt.getSubject());
        if (staff.isPresent()) {
            User user = staff.get();
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new DisabledException("This account has been deactivated");
            }
            List<GrantedAuthority> authorities = new ArrayList<>();


            user.getRole().getPermissions()
                    .forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
            return new KeycloakAuthenticationToken(jwt, user.getEmail(), user.getId(), null, authorities);
        }
        Customer customer = customerProvisioningService.getOrCreateCustomer(jwt);
        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            throw new DisabledException("This account has been deactivated");

        }

        return new KeycloakAuthenticationToken(jwt, customer.getId(), null, customer.getId(),
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }
}
