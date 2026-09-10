package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogService {

    void log(
            AuditEventType eventType,
            String email,
            boolean success,
            String details,
            HttpServletRequest request
    );

    boolean isIpSuspicious(String ipAddress);
}