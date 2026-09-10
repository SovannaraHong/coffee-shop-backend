package com.coffee_shop.coffee_shop.entity;

import com.coffee_shop.coffee_shop.util.enums.AuditEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_email", columnList = "email"),
        @Index(name = "idx_audit_event_type", columnList = "event_type"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    @Column(length = 150)
    private String email;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(length = 500)
    private String details;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}
