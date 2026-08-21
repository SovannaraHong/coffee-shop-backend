package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.RoleCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.RoleUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.RoleResponse;
import com.coffee_shop.coffee_shop.entity.Permission;
import com.coffee_shop.coffee_shop.entity.Role;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.RoleMapper;
import com.coffee_shop.coffee_shop.repository.PermissionRepository;
import com.coffee_shop.coffee_shop.repository.RoleRepository;
import com.coffee_shop.coffee_shop.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse create(RoleCreateRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new BadRequestException("Role already exists: " + request.getName());
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null) {
            for (Long permissionId : request.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> ResourceNotFoundException.notFoundException("Permission", permissionId));
                permissions.add(permission);
            }
        }

        Role role = Role.builder()
                .name(request.getName())
                .permissions(permissions)
                .build();

        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse assignPermission(Long roleId, Long permissionId) {
        Role role = findRoleEntity(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Permission", permissionId));
        role.getPermissions().add(permission);
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse removePermission(Long roleId, Long permissionId) {
        Role role = findRoleEntity(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Permission", permissionId));
        role.getPermissions().remove(permission);
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    public RoleResponse update(RoleUpdateRequest request, Long roleId) {
        Role roleEntity = findRoleEntity(roleId);
        roleMapper.updateEntity(roleEntity, request);
        return roleMapper.toResponse(roleRepository.save(roleEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return roleMapper.toResponse(findRoleEntity(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        roleRepository.delete(findRoleEntity(id));
    }

    private Role findRoleEntity(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Role", id));
    }
}