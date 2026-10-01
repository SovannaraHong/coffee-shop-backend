package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.repository.CustomerRepository;
import com.coffee_shop.coffee_shop.service.CustomerLoginAttemptService;
import com.coffee_shop.coffee_shop.util.LockoutPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerLoginAttemptServiceImpl implements CustomerLoginAttemptService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(Long customerId) {
        customerRepository.findByIdForUpdate(customerId).ifPresent(customer -> {
            int attempts = customer.getFailedLoginAttempts() + 1;
            customer.setFailedLoginAttempts(attempts);

            if (attempts >= LockoutPolicy.MAX_FAILED_ATTEMPTS) {
                long minutes = LockoutPolicy.minutesForStage(customer.getLockStage());
                customer.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
                customer.setLockStage(LockoutPolicy.nextStage(customer.getLockStage()));
                customer.setFailedLoginAttempts(0);
            }
            customerRepository.save(customer);
        });
    }

    @Override
    @Transactional
    public void resetAttempts(Long customerId) {
        customerRepository.findByIdForUpdate(customerId).ifPresent(customer -> {
            // Skip the write on the normal happy path (nothing to reset).
            if (customer.getFailedLoginAttempts() > 0
                    || customer.getLockedUntil() != null
                    || customer.getLockStage() > 0) {
                customer.setFailedLoginAttempts(0);
                customer.setLockedUntil(null);
                customer.setLockStage(0);
                customerRepository.save(customer);
            }
        });
    }
}