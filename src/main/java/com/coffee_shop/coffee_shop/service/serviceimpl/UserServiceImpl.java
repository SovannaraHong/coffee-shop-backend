package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.UserCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.UserUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.UserResponse;
import com.coffee_shop.coffee_shop.entity.Role;
import com.coffee_shop.coffee_shop.entity.User;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.UserMapper;
import com.coffee_shop.coffee_shop.repository.RoleRepository;
import com.coffee_shop.coffee_shop.repository.UserRepository;

import com.coffee_shop.coffee_shop.security.keycloak.KeyCloakAdminService;
import com.coffee_shop.coffee_shop.service.AuditLogService;
import com.coffee_shop.coffee_shop.service.S3Service;
import com.coffee_shop.coffee_shop.service.UserService;
import com.coffee_shop.coffee_shop.specification.user.UserFilter;
import com.coffee_shop.coffee_shop.specification.user.UserSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final AuditLogService auditLogService;
    private final KeyCloakAdminService keycloakAdminService;

    @Override
    @Transactional
    public UserResponse createStaff(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Role", request.getRoleId()));

        String[] name = splitName(request.getFullName());
        // The staff member sets their own password + authenticator-app code from the e-mailed link.
        String keycloakId = keycloakAdminService.createUser(request.getEmail(), name[0], name[1],
                List.of("VERIFY_EMAIL", "UPDATE_PASSWORD", "CONFIGURE_TOTP"));
        try {
            User saved = userRepository.save(User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .keycloakId(keycloakId)
                    .role(role)
                    .isActive(true)
                    .build());
            auditLogService.log(AuditEventType.STAFF_CREATED, saved.getEmail(), true,
                    "Created with role: " + role.getName(), null);
            return userMapper.toResponse(saved);
        } catch (RuntimeException e) {
            keycloakAdminService.deleteUser(keycloakId);   // no orphan Keycloak account
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponse updateStaff(Long id, UserUpdateRequest request) {
        User user = findUserEntityById(id);
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already registered");
            }
            if (user.getKeycloakId() != null) {
                keycloakAdminService.changeEmail(user.getKeycloakId(), request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> ResourceNotFoundException.notFoundException("Role", request.getRoleId()));
            user.setRole(role);
            auditLogService.log(AuditEventType.ROLE_PERMISSION_CHANGED, user.getEmail(), true,
                    "Role changed to: " + role.getName(), null);
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findUserEntityById(id);
        userRepository.delete(user);
        userRepository.flush();   // fails here (e.g. FK) before Keycloak is touched
        if (user.getKeycloakId() != null) {
            keycloakAdminService.deleteUser(user.getKeycloakId());
        }
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<UserResponse> getPagination(Map<String, String> params) {
        UserFilter filter = new UserFilter();
        if (params.containsKey("fullName")) filter.setFullName(params.get("fullName"));
        if (params.containsKey("email")) filter.setEmail(params.get("email"));
        if (params.containsKey("isActive")) filter.setIsActive(Boolean.parseBoolean(params.get("isActive")));
        if (params.containsKey("roleId")) filter.setRoleId(Long.parseLong(params.get("roleId")));

        UserSpec spec = new UserSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);
        Page<UserResponse> page = userRepository.findAll(spec, pageable).map(userMapper::toResponse);
        return new PageDTO<>(page);
    }

    @Override
    public UserResponse uploadImage(Long id, MultipartFile file) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("User", id));
        if (user.getAvatarUrl() != null && user.getAvatarUrl().startsWith("https://")) {
            s3Service.deleteFile(user.getAvatarUrl());
        }
        String imageUrl = s3Service.uploadFile(file, "user_images");
        user.setAvatarUrl(imageUrl);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse unlockAccount(Long id) {
        User user = findUserEntityById(id);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLockStage(0);
        if (user.getKeycloakId() != null) {
            keycloakAdminService.clearBruteForce(user.getKeycloakId());
        }
        auditLogService.log(AuditEventType.ACCOUNT_LOCKED, user.getEmail(), true,
                "Account manually unlocked by admin", null);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse changeStatus(Long id) {
        User user = findUserEntityById(id);
        user.setIsActive(!user.getIsActive());
        User saved = userRepository.save(user);
        if (saved.getKeycloakId() != null) {
            keycloakAdminService.setEnabled(saved.getKeycloakId(), saved.getIsActive());
        }
        auditLogService.log(AuditEventType.ACCOUNT_STATUS_CHANGED, saved.getEmail(), true,
                "isActive set to: " + saved.getIsActive(), null);
        return userMapper.toResponse(saved);
    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("User", id));
    }

    private static String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"", ""};
        String[] parts = fullName.trim().split("\\s+", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }
}