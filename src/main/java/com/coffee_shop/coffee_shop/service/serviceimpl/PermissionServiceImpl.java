package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.PermissionCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.PermissionUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PermissionResponse;
import com.coffee_shop.coffee_shop.entity.Permission;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.PermissionMapper;
import com.coffee_shop.coffee_shop.repository.PermissionRepository;
import com.coffee_shop.coffee_shop.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public PermissionResponse create(PermissionCreateRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new BadRequestException("Permission already exists: " + request.getName());
        }
        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Override
    public PermissionResponse update(Long id, PermissionUpdateRequest request) {
        Permission permissionId = getById(id);
        permissionMapper.updateEntity(permissionId, request);
        return permissionMapper.toResponse(permissionRepository.save(permissionId));

    }

    @Override
    @Transactional
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Permission", id));
        permissionRepository.delete(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponse)
                .toList();
    }

    private Permission getById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Permission", id));
    }
}