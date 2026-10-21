package com.coffee_shop.coffee_shop.security.keycloak;

import com.coffee_shop.coffee_shop.repository.OrderRepository;
import com.coffee_shop.coffee_shop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ownership checks for customers. Customer principal = Long customerId (see KeycloakJwtConverter).
 */
@Component("accessGuard")
@RequiredArgsConstructor
public class AccessGuard {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public boolean isOwnCustomer(Long customerId, Authentication authentication) {
        return authentication != null
                && authentication.getPrincipal() instanceof Long self
                && self.equals(customerId);
    }

    @Transactional(readOnly = true)
    public boolean ownsOrder(Long orderId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long self)) {
            return false;
        }
        return orderRepository.findById(orderId)
                .map(o -> o.getCustomer() != null && self.equals(o.getCustomer().getId()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean ownsPayment(Long paymentId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long self)) {
            return false;
        }
        return paymentRepository.findById(paymentId)
                .map(p -> p.getOrder() != null && p.getOrder().getCustomer() != null
                        && self.equals(p.getOrder().getCustomer().getId()))
                .orElse(false);
    }
}