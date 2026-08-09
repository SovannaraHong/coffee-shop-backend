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
import com.coffee_shop.coffee_shop.mapper.CustomerMapper;
import com.coffee_shop.coffee_shop.repository.CustomerRepository;
import com.coffee_shop.coffee_shop.service.CustomerService;
import com.coffee_shop.coffee_shop.service.OtpService;
import com.coffee_shop.coffee_shop.util.JwtUtil;
import com.coffee_shop.coffee_shop.util.enums.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;


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

    @Transactional(readOnly = true)
    @Override
    public LoginResponse login(CustomerLoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (customer.getPassword() == null) {
            throw new BadRequestException("This account uses social login. Please sign in with Google/Facebook.");
        }
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        if (!customer.getIsVerified()) {
            throw new BadRequestException("Please verify your email before logging in");
        }

        if (!customer.getIsActive()) {
            throw new BadRequestException("This account has been deactivated");
        }
        String token = jwtUtil.generateToken(customer.getId(), customer.getEmail());

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
