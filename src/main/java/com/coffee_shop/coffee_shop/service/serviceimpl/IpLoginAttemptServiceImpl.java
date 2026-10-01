package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.entity.IpLoginAttempt;
import com.coffee_shop.coffee_shop.exception.TooManyRequestsException;
import com.coffee_shop.coffee_shop.repository.IpLoginAttemptRepository;
import com.coffee_shop.coffee_shop.service.IpLoginAttemptService;
import com.coffee_shop.coffee_shop.util.LockoutPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IpLoginAttemptServiceImpl implements IpLoginAttemptService {

    private final IpLoginAttemptRepository ipLoginAttemptRepository;

    private static final int MAX_FAILED_ATTEMPTS = 20;
    private static final int LOCKOUT_HOURS = 1;

    @Override
    @Transactional(readOnly = true)
    public void checkNotBanned(String ip) {
        ipLoginAttemptRepository.findById(ip).ifPresent(attempt -> {
            LocalDateTime now = LocalDateTime.now();

            if (attempt.getLockedUntil() != null && attempt.getLockedUntil().isAfter(now)) {

                long seconds = Duration.between(now, attempt.getLockedUntil()).toSeconds();

                throw new TooManyRequestsException(          // was BadRequestException (400)
                        TooManyRequestsException.IP_BANNED,
                        "Too many failed attempts from this network. Please try again later.",
                        seconds);
            }
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(String ip) {
        try {
            // Row lock: parallel requests can no longer overwrite each other's increments.
            IpLoginAttempt attempt = ipLoginAttemptRepository.findByIdForUpdate(ip)
                    .orElseGet(() -> IpLoginAttempt.builder().ipAddress(ip).failedAttempts(0).lockStage(0).build());

            int attempts = attempt.getFailedAttempts() + 1;
            attempt.setFailedAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                attempt.setLockedUntil(LocalDateTime.now().plusHours(LOCKOUT_HOURS));
                attempt.setLockStage(LockoutPolicy.nextStage(attempt.getLockStage()));
                attempt.setFailedAttempts(0);
            }
            ipLoginAttemptRepository.save(attempt);
        } catch (DataIntegrityViolationException ignored) {
            // Two first-ever failures from the same IP raced on INSERT. Losing one count is harmless.
        }
    }

    @Override
    @Transactional
    public void resetAttempts(String ip) {
        ipLoginAttemptRepository.findById(ip).ifPresent(attempt -> {
            attempt.setFailedAttempts(0);
            attempt.setLockedUntil(null);
            attempt.setFailedAttempts(0);
            attempt.setLockStage(0);
            ipLoginAttemptRepository.save(attempt);
        });
    }
}