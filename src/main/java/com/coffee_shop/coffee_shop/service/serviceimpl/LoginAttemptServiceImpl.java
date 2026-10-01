package com.coffee_shop.coffee_shop.service.serviceimpl;//package com.coffee_shop.coffee_shop.service.serviceimpl;
//
//import com.coffee_shop.coffee_shop.entity.User;
//import com.coffee_shop.coffee_shop.repository.UserRepository;
//import com.coffee_shop.coffee_shop.service.LoginAttemptService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class LoginAttemptServiceImpl implements LoginAttemptService {
//
//    private final UserRepository userRepository;
//
//    private static final int MAX_FAILED_ATTEMPTS = 5;
//    private static final int LOCKOUT_HOURS = 24;
//
//    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void registerFailedAttempt(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        int attempts = user.getFailedLoginAttempts() + 1;
//
//        user.setFailedLoginAttempts(attempts);
//
//        if (attempts >= MAX_FAILED_ATTEMPTS) {
//            user.setLockedUntil(
//                    LocalDateTime.now().plusHours(LOCKOUT_HOURS)
//            );
//        }
//
//        userRepository.save(user);
//    }
//
//    @Override
//    public void resetAttempts(Long userId) {
//        userRepository.findById(userId).ifPresent(user -> {
//            user.setFailedLoginAttempts(0);
//            user.setLockedUntil(null);
//            user.setLockStage(0);
//            userRepository.save(user);
//        });
//    }
//}

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

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int stage = user.getLockStage();

        if (stage == 0) {

            int attempts = user.getFailedLoginAttempts() + 1;

            user.setFailedLoginAttempts(attempts);

            // First 5 wrong attempts -> lock 1 minute
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lock(user, 1, 1);
            }

        } else if (stage == 1) {

            // After 1 minute -> one wrong attempt -> lock 5 minutes
            lock(user, 5, 2);

        } else if (stage == 2) {

            // After 5 minutes -> one wrong attempt -> lock 1 hour
            lock(user, 60, 3);

        } else {

            // After 1 hour -> one wrong attempt -> lock 24 hours
            lock(user, 24 * 60, 4);
        }

        userRepository.save(user);
    }

    private void lock(User user, long minutes, int nextStage) {
        user.setLockedUntil(
                LocalDateTime.now().plusMinutes(minutes)
        );

        user.setLockStage(nextStage);
        user.setFailedLoginAttempts(0);
    }

    @Override
    @Transactional
    public void resetAttempts(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLockStage(0);

        userRepository.save(user);
    }
}
