package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.service.IpLoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ip-login-attempts")
@RequiredArgsConstructor
public class IpLoginAttemptController {

    private final IpLoginAttemptService ipLoginAttemptService;


    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @DeleteMapping("/{ip}/reset")
    public ResponseEntity<Void> resetAttempts(
            @PathVariable String ip) {

        ipLoginAttemptService.resetAttempts(ip);

        return ResponseEntity.noContent().build();
    }
}
