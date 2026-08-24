package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.RefreshTokenRequest;
import com.coffee_shop.coffee_shop.dto.request.UserCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.UserLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.VerifyOtpRequest;
import com.coffee_shop.coffee_shop.dto.response.StaffTokenResponse;
import com.coffee_shop.coffee_shop.dto.response.UserResponse;
import com.coffee_shop.coffee_shop.entity.Role;
import com.coffee_shop.coffee_shop.entity.User;
import com.coffee_shop.coffee_shop.entity.UserSession;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.UserMapper;
import com.coffee_shop.coffee_shop.repository.RoleRepository;
import com.coffee_shop.coffee_shop.repository.UserRepository;
import com.coffee_shop.coffee_shop.repository.UserSessionRepository;
import com.coffee_shop.coffee_shop.security.auth.AuthUser;
import com.coffee_shop.coffee_shop.service.JwtService;
import com.coffee_shop.coffee_shop.service.LoginAttemptService;
import com.coffee_shop.coffee_shop.service.OtpService;
import com.coffee_shop.coffee_shop.service.UserService;
import com.coffee_shop.coffee_shop.util.DeviceFingerprintUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final UserSessionRepository userSessionRepository;
    private final LoginAttemptService loginAttemptService;


    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_HOURS = 24;

    @Override
    @Transactional
    public UserResponse createStaff(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Role", request.getRoleId()));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void login(UserLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getLockedUntil());
            throw new BadRequestException(
                    "Account locked due to too many failed attempts. Try again in " + minutesLeft + " minutes."
            );
        }


        try {
            // delegates to DaoAuthenticationProvider -> CustomUserDetailsService + PasswordEncoder
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            loginAttemptService.registerFailedAttempt(user.getId());
            // never reveal which one it was — always the same generic message
            throw new BadRequestException("Invalid email or password");
        } catch (DisabledException e) {
            throw new BadRequestException("This account has been deactivated");
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        otpService.generateAndSendOtp(request.getEmail());
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        String sessionId = jwtService.extractSessionId(accessToken);
        UserSession session = userSessionRepository.findBySessionIdAndRevokedFalse(sessionId)
                .orElseThrow(() -> new BadRequestException("Session not found or already logged out"));
        session.setRevoked(true);
        userSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void logoutAllDevices(String accessToken) {
        Long userId = jwtService.extractClaims(accessToken).get("userId", Long.class);
        List<UserSession> sessions = userSessionRepository.findAllByUserIdAndRevokedFalse(userId);
        sessions.forEach(s -> s.setRevoked(true));
        userSessionRepository.saveAll(sessions);
    }

    @Override
    @Transactional
    public StaffTokenResponse verifyLoginOtp(VerifyOtpRequest request, HttpServletRequest httpServletRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email"));
        if (!user.getIsActive()) {
            throw new BadRequestException("This account has been deactivated");
        }
        otpService.verifyOtp(request.getEmail(), request.getCode());

        String userAgent = DeviceFingerprintUtil.extractDeviceInfo(httpServletRequest);
        String ip = DeviceFingerprintUtil.extractIp(httpServletRequest);
        String fingerprint = DeviceFingerprintUtil.fingerprint(userAgent, ip);
        String sessionId = UUID.randomUUID().toString();

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .user(user)
                .deviceInfo(userAgent)
                .ipAddress(ip)
                .createdAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .revoked(false)
                .build();
        userSessionRepository.save(session);

        AuthUser authUser = new AuthUser(user);
        String accessToken = jwtService.generateAccessToken(authUser, sessionId, fingerprint);
        String refreshToken = jwtService.generateRefreshToken(authUser, sessionId, fingerprint);

        return StaffTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public StaffTokenResponse refresh(RefreshTokenRequest request, HttpServletRequest httpServletRequest) {
        String token = request.getRefreshToken();
        if (!jwtService.isTokenValid(token) || !jwtService.isRefreshToken(token)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String sessionId = jwtService.extractSessionId(token);
        UserSession session = userSessionRepository.findBySessionIdAndRevokedFalse(sessionId)
                .orElseThrow(() -> new BadRequestException("Session has been revoked. Please log in again."));

        String currentFingerprint = DeviceFingerprintUtil.fingerprint(
                DeviceFingerprintUtil.extractDeviceInfo(httpServletRequest),
                DeviceFingerprintUtil.extractIp(httpServletRequest)
        );
        if (!currentFingerprint.equals(jwtService.extractDeviceFingerprint(token))) {
            throw new BadRequestException("Refresh token cannot be used from a different device.");
        }

        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (!user.getIsActive()) {
            throw new BadRequestException("This account has been deactivated");
        }

        session.setLastUsedAt(LocalDateTime.now());
        userSessionRepository.save(session);

        AuthUser authUser = new AuthUser(user);
        String newAccessToken = jwtService.generateAccessToken(authUser, sessionId, currentFingerprint);
        String newRefreshToken = jwtService.generateRefreshToken(authUser, sessionId, currentFingerprint);

        return StaffTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse changeStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("User", id));
        user.setIsActive(!user.getIsActive());
        return userMapper.toResponse(userRepository.save(user));
    }
}