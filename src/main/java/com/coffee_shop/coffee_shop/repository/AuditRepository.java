package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.AuditLog;
import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    List<AuditLog> findByEmailAndEventTypeAndCreatedAtAfter
            (String email, AuditEventType eventType, LocalDateTime after);

    Long countByIpAddressAndEventTypeAndCreatedAtAfter
            (String ipAddress, AuditEventType eventType, LocalDateTime after);
}
