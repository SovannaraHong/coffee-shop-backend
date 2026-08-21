package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.RoleCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.RoleResponse;
import com.coffee_shop.coffee_shop.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/roles")
@RestController
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleResponse> assignPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.assignPermission(roleId, permissionId));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleResponse> removePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.removePermission(roleId, permissionId));
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAll() {
        return ResponseEntity.ok(roleService.getAll());
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}