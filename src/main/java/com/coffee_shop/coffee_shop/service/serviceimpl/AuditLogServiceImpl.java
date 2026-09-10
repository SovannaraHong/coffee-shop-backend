package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.entity.AuditLog;
import com.coffee_shop.coffee_shop.repository.AuditRepository;
import com.coffee_shop.coffee_shop.service.AuditLogService;
import com.coffee_shop.coffee_shop.util.DeviceFingerprintUtil;
import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditRepository auditRepository;
    private static final int SUSPICIOUS_IP_THRESHOLD = 20;
    private static final int SUSPICIOUS_WINDOW_MINUTES = 15;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void log(AuditEventType eventType, String email, boolean success, String details, HttpServletRequest request) {

        String ip = request != null ? DeviceFingerprintUtil.extractIp(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        AuditLog auditLog = AuditLog.builder()
                .eventType(eventType)
                .email(email)
                .success(success)
                .details(details)
                .ipAddress(ip)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

        auditRepository.save(auditLog);

    }

    @Transactional(readOnly = true)
    @Override
    public boolean isIpSuspicious(String ipAddress) {

        if (ipAddress == null) return false;
        LocalDateTime since = LocalDateTime.now().minusMinutes(SUSPICIOUS_WINDOW_MINUTES);
        Long count = auditRepository.countByIpAddressAndEventTypeAndCreatedAtAfter(ipAddress, AuditEventType.LOGIN_FAILED, since);
        return count >= SUSPICIOUS_IP_THRESHOLD;
    }
}
