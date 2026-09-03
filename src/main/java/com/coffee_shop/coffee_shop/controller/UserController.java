package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.UserCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.UserUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.UserResponse;
import com.coffee_shop.coffee_shop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequestMapping("api/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createStaff(request));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<UserResponse>> getPagination(
            @RequestParam Map<String, String> params) {

        return ResponseEntity.ok(
                userService.getPagination(params)
        );
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> changeStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.changeStatus(id));
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {

        return ResponseEntity.ok(
                userService.updateStaff(id, request)
        );
    }


    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/{id}/image")
    public ResponseEntity<UserResponse> uploadUserImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        return ResponseEntity.ok(
                userService.uploadImage(id, file)
        );
    }


    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<UserResponse> unlockAccount(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unlockAccount(id));
    }
}