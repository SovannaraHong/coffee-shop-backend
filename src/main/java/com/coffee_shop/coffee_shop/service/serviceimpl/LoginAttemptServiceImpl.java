package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.entity.User;
import com.coffee_shop.coffee_shop.repository.UserRepository;
import com.coffee_shop.coffee_shop.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_HOURS = 24;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int attempts = user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(
                    LocalDateTime.now().plusHours(LOCKOUT_HOURS)
            );
        }

        userRepository.save(user);
    }
}
