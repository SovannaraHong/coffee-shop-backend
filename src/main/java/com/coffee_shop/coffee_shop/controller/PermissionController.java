package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.PermissionCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.PermissionResponse;
import com.coffee_shop.coffee_shop.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/permissions")
@RestController
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @PostMapping
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(request));
    }

    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAll() {
        return ResponseEntity.ok(permissionService.getAll());
    }
}