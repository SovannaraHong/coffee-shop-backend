package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.RefreshTokenRequest;
import com.coffee_shop.coffee_shop.dto.request.UserCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.UserLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.VerifyOtpRequest;
import com.coffee_shop.coffee_shop.dto.response.StaffTokenResponse;
import com.coffee_shop.coffee_shop.dto.response.UserResponse;
import com.coffee_shop.coffee_shop.entity.Role;
import com.coffee_shop.coffee_shop.entity.User;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.UserMapper;
import com.coffee_shop.coffee_shop.repository.RoleRepository;
import com.coffee_shop.coffee_shop.repository.UserRepository;

import com.coffee_shop.coffee_shop.security.auth.AuthUser;
import com.coffee_shop.coffee_shop.service.JwtService;
import com.coffee_shop.coffee_shop.service.OtpService;
import com.coffee_shop.coffee_shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @Transactional(readOnly = true)
    public void login(UserLoginRequest request) {
        try {
            // delegates to DaoAuthenticationProvider -> CustomUserDetailsService + PasswordEncoder
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            // never reveal which one it was — always the same generic message
            throw new BadRequestException("Invalid email or password");
        } catch (DisabledException e) {
            throw new BadRequestException("This account has been deactivated");
        }

        otpService.generateAndSendOtp(request.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffTokenResponse verifyLoginOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email"));

        if (!user.getIsActive()) {
            throw new BadRequestException("This account has been deactivated");
        }

        otpService.verifyOtp(request.getEmail(), request.getCode());

        AuthUser authUser = new AuthUser(user);
        String accessToken = jwtService.generateAccessToken(authUser);
        String refreshToken = jwtService.generateRefreshToken(authUser);

        return StaffTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffTokenResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtService.isTokenValid(token) || !jwtService.isRefreshToken(token)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (!user.getIsActive()) {
            throw new BadRequestException("This account has been deactivated");
        }

        AuthUser authUser = new AuthUser(user);
        String newAccessToken = jwtService.generateAccessToken(authUser);
        String newRefreshToken = jwtService.generateRefreshToken(authUser);

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