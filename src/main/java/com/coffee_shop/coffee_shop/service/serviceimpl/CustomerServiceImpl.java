package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.CustomerLoginRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerRegisterRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerUpdateRequest;
import com.coffee_shop.coffee_shop.dto.request.VerifyOtpRequest;
import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;
import com.coffee_shop.coffee_shop.dto.response.LoginResponse;
import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.exception.TooManyRequestsException;
import com.coffee_shop.coffee_shop.mapper.CustomerMapper;
import com.coffee_shop.coffee_shop.repository.CustomerRepository;
import com.coffee_shop.coffee_shop.service.*;
import com.coffee_shop.coffee_shop.util.DeviceFingerprintUtil;
import com.coffee_shop.coffee_shop.util.enums.AuthProvider;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private static final String INVALID = "Invalid email or password";

    private String dummyHash;   // not final, so Lombok leaves it out of the constructor

    @PostConstruct
    void init() {
        dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtService jwtService; // was JwtUtil — now unified
    private final IpLoginAttemptService ipLoginAttemptService;
    private final CustomerLoginAttemptService customerLoginAttemptService;

    @Transactional
    @Override
    public CustomerResponse register(CustomerRegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw BadRequestException.alreadyExits("Customer", request.getEmail());
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .authProvider(AuthProvider.LOCAL)
                .isVerified(false)
                .isActive(true)
                .build();
        Customer save = customerRepository.save(customer);
        otpService.generateAndSendOtp(customer.getEmail());

        return mapper.toResponse(save);
    }

    @Transactional
    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Customer"));
        if (customer.getIsVerified()) {
            throw new BadRequestException("Account is already verified");
        }
        otpService.verifyOtp(request.getEmail(), request.getCode());
        customer.setIsVerified(true);
        customerRepository.save(customer);
    }

    @Transactional
    @Override
    public LoginResponse login(CustomerLoginRequest request, HttpServletRequest httpServletRequest) {
        String ip = DeviceFingerprintUtil.extractIp(httpServletRequest);
        ipLoginAttemptService.checkNotBanned(ip); // 429 IP_BANNED

        Customer customer = customerRepository.findByEmail(request.getEmail()).orElse(null);

        if (customer == null) {
            passwordEncoder.matches(request.getPassword(), dummyHash);  // same response time as a real check
            ipLoginAttemptService.registerFailedAttempt(ip);            // unknown emails count against the IP
            throw new BadRequestException(INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        if (customer.getLockedUntil() != null && customer.getLockedUntil().isAfter(now)) {
            long seconds = Math.max(1, ChronoUnit.SECONDS.between(now, customer.getLockedUntil()));
            long minutes = (seconds + 59) / 60;
            throw new TooManyRequestsException(
                    TooManyRequestsException.ACCOUNT_LOCKED,
                    "Account locked due to too many failed attempts. Try again in "
                            + minutes + (minutes == 1 ? " minute." : " minutes."),
                    seconds);
        }

        if (customer.getPassword() == null) {
            throw new BadRequestException("This account uses social login. Please sign in with Google/Facebook.");
        }

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            customerLoginAttemptService.registerFailedAttempt(customer.getId()); // fixed: was missing
            ipLoginAttemptService.registerFailedAttempt(ip);                    // fixed: was missing
            throw new BadRequestException(INVALID);
        }
        if (!customer.getIsVerified()) {
            throw new BadRequestException("Please verify your email before logging in");
        }
        if (!customer.getIsActive()) {
            throw new BadRequestException("This account has been deactivated");
        }

        customerLoginAttemptService.resetAttempts(customer.getId()); // fixed: correct service now
//        ipLoginAttemptService.resetAttempts(ip);

        String token = jwtService.generateCustomerToken(customer.getId(), customer.getEmail());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .customer(mapper.toResponse(customer))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getProfile(Long id) {
        return mapper.toResponse(findRequired(id));
    }

    @Override
    @Transactional
    public CustomerResponse updateProfile(Long id, CustomerUpdateRequest request) {
        Customer customer = findRequired(id);
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        return mapper.toResponse(customerRepository.save(customer));
    }

    private Customer findRequired(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Customer", id));
    }
}