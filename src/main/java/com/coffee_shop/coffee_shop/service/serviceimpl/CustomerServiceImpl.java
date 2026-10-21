package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.ChangePasswordRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerRegisterRequest;
import com.coffee_shop.coffee_shop.dto.request.CustomerUpdateRequest;
import com.coffee_shop.coffee_shop.dto.request.ForgotPasswordRequest;
import com.coffee_shop.coffee_shop.dto.response.CustomerResponse;
import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.CustomerMapper;
import com.coffee_shop.coffee_shop.repository.CustomerRepository;
import com.coffee_shop.coffee_shop.security.keycloak.KeyCloakAdminService;
import com.coffee_shop.coffee_shop.service.CustomerService;
import com.coffee_shop.coffee_shop.util.enums.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;
    // ADDED (2026-10-08): needed for customer self-registration via Keycloak
    private final KeyCloakAdminService keycloakAdminService;

    /**
     * ADDED (2026-10-08): Customer self-registration flow.
     *
     * Flow:
     *   1. Check email not already taken in our DB.
     *   2. Create user in Keycloak with the customer's own password + send VERIFY_EMAIL.
     *   3. Save customer record in our DB (isVerified=false until email is clicked).
     *   4. On DB failure → delete Keycloak user to avoid orphaned accounts.
     *
     * After clicking the verify link (Keycloak page, one click):
     *   - Customer logs in → CustomerProvisioningService.getOrCreateCustomer() sets isVerified=true.
     */
    @Override
    @Transactional
    public CustomerResponse register(CustomerRegisterRequest request) {
        // Guard: check our DB first to give a fast, clear error before touching Keycloak
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // Create in Keycloak: sets password + queues VERIFY_EMAIL action
        String keycloakId = keycloakAdminService.registerCustomer(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
        );

        try {
            // Save to our DB — isVerified stays false until the customer clicks the email link.
            // CustomerProvisioningService.getOrCreateCustomer() will flip it to true on first login.
            Customer saved = customerRepository.save(Customer.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())               // optional — null if customer skipped it
                    .keycloakId(keycloakId)
                    .authProvider(AuthProvider.LOCAL)        // registered with email+password (not Google/Facebook)
                    .isVerified(false)
                    .isActive(true)
                    .build());
            return mapper.toResponse(saved);
        } catch (RuntimeException e) {
            // Rollback: delete the Keycloak user so we don't leave an orphan account
            keycloakAdminService.deleteUser(keycloakId);
            throw e;
        }
    }

    /**
     * ADDED (2026-10-08): Forgot-password flow.
     * Delegates entirely to Keycloak — no DB changes needed.
     * Keycloak searches by email, sends UPDATE_PASSWORD email with a 1-day link.
     * We always return 200 OK even if email not found (no email enumeration).
     */
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Delegates to KeyCloakAdminService.sendForgotPasswordEmail()
        // which silently does nothing if the email is not registered in Keycloak
        keycloakAdminService.sendForgotPasswordEmail(request.getEmail());
    }

    /**
     * ADDED (2026-10-08): Change-password flow for logged-in customer.
     *
     * Flow:
     *   1. Verify currentPassword is correct by trying a Keycloak token grant.
     *      If wrong → Keycloak returns 401 → we throw BadRequestException.
     *   2. Call Keycloak reset-password API with the new password.
     *
     * @param keycloakId the customer's Keycloak UUID (JWT "sub" claim, passed from controller)
     * @param request    contains currentPassword (for verification) and newPassword
     */
    @Override
    public void changePassword(String keycloakId, ChangePasswordRequest request) {
        // Find the customer in our DB to get their email (needed to verify current password)
        Customer customer = customerRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BadRequestException("Customer not found"));

        // Verify the current password is correct before allowing a change
        // This prevents someone from changing the password on an unlocked phone
        boolean valid = keycloakAdminService.verifyPassword(customer.getEmail(), request.getCurrentPassword());
        if (!valid) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Current password confirmed — update to the new password in Keycloak
        keycloakAdminService.resetPassword(keycloakId, request.getNewPassword());
    }

    // Returns the logged-in customer's profile — called by GET /api/customers/me
    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getProfile(Long id) {
        return mapper.toResponse(findRequired(id));
    }

    // Updates the logged-in customer's own profile — called by PUT /api/customers/me
    @Override
    @Transactional
    public CustomerResponse updateProfile(Long id, CustomerUpdateRequest request) {
        Customer customer = findRequired(id);
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        return mapper.toResponse(customerRepository.save(customer));
    }

    // Shared helper — throws 404 if customer not found by id
    private Customer findRequired(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Customer", id));
    }
}