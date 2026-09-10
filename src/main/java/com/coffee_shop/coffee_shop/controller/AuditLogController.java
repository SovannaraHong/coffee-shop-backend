package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.response.AuditLogResponse;
import com.coffee_shop.coffee_shop.entity.AuditLog;
import com.coffee_shop.coffee_shop.repository.AuditRepository;
import com.coffee_shop.coffee_shop.specification.auditLog.AuditLogFilter;
import com.coffee_shop.coffee_shop.specification.auditLog.AuditLogSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RequestMapping("api/audit-logs")
@RestController
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditRepository auditLogRepository;

    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    @GetMapping
    public ResponseEntity<PageDTO<AuditLogResponse>> search(@RequestParam Map<String, String> params) {
        AuditLogFilter filter = new AuditLogFilter();
        if (params.containsKey("email")) filter.setEmail(params.get("email"));
        if (params.containsKey("eventType"))
            filter.setEventType(AuditEventType.valueOf(params.get("eventType").toUpperCase()));
        if (params.containsKey("success")) filter.setSuccess(Boolean.valueOf(params.get("success")));
        if (params.containsKey("ipAddress")) filter.setIpAddress(params.get("ipAddress"));
        if (params.containsKey("fromDate")) filter.setFromDate(LocalDateTime.parse(params.get("fromDate")));
        if (params.containsKey("toDate")) filter.setToDate(LocalDateTime.parse(params.get("toDate")));

        AuditLogSpec spec = new AuditLogSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);

        Page<AuditLogResponse> page = auditLogRepository.findAll(spec, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(new PageDTO<>(page));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .eventType(log.getEventType())
                .email(log.getEmail())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .details(log.getDetails())
                .success(log.getSuccess())
                .createdAt(log.getCreatedAt())
                .build();
    }
}