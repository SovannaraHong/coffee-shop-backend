package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.entity.IpLoginAttempt;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.repository.IpLoginAttemptRepository;
import com.coffee_shop.coffee_shop.service.IpLoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class IpLoginAttemptServiceImpl implements IpLoginAttemptService {

    private final IpLoginAttemptRepository ipLoginAttemptRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_HOURS = 1;

    @Override
    @Transactional(readOnly = true)
    public void checkNotBanned(String ip) {
        ipLoginAttemptRepository.findById(ip).ifPresent(attempt -> {
            if (attempt.getLockedUntil() != null && attempt.getLockedUntil().isAfter(LocalDateTime.now())) {
                long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), attempt.getLockedUntil());
                throw new BadRequestException(
                        "Too many failed attempts from this network. Try again in " + minutesLeft + " minute(s)."
                );
            }
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(String ip) {
        IpLoginAttempt attempt = ipLoginAttemptRepository.findById(ip)
                .orElse(IpLoginAttempt.builder().ipAddress(ip).failedAttempts(0).build());

        int attempts = attempt.getFailedAttempts() + 1;
        attempt.setFailedAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            attempt.setLockedUntil(LocalDateTime.now().plusHours(LOCKOUT_HOURS));
        }
        ipLoginAttemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public void resetAttempts(String ip) {
        ipLoginAttemptRepository.findById(ip).ifPresent(attempt -> {
            attempt.setFailedAttempts(0);
            attempt.setLockedUntil(null);
            ipLoginAttemptRepository.save(attempt);
        });
    }
}