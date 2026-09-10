package com.coffee_shop.coffee_shop.dto.response;

import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private AuditEventType eventType;
    private String email;
    private String ipAddress;
    private String userAgent;
    private String details;
    private Boolean success;
    private LocalDateTime createdAt;
}