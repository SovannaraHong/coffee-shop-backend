package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ip_login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpLoginAttempt {

    @Id
    private String ipAddress;

    @Builder.Default
    private int failedAttempts = 0;

    private LocalDateTime lockedUntil;
}