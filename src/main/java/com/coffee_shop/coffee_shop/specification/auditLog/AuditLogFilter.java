package com.coffee_shop.coffee_shop.specification.auditLog;

import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogFilter {
    private String email;
    private AuditEventType eventType;
    private Boolean success;
    private String ipAddress;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}