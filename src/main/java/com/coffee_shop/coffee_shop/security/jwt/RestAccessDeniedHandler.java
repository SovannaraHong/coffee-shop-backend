package com.coffee_shop.coffee_shop.security.jwt;

import com.coffee_shop.coffee_shop.service.AuditLogService;
import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditLogService auditLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {

        String principal = SecurityContextHolder.getContext().getAuthentication() != null ?
                String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal()) : "unknow";
        auditLogService.log(AuditEventType.PERMISSION_DENIED, principal,
                false, "Attempted: " + request.getMethod() + " " + request.getRequestURI(), request);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "status", 403,
                "message", "You do not have permission to perform this action.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}