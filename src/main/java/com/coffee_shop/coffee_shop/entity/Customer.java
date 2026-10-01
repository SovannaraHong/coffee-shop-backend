package com.coffee_shop.coffee_shop.entity;

import com.coffee_shop.coffee_shop.util.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 150)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // null when the customer only ever used Google/Facebook login
    @Column(length = 255)
    private String password;

    @Column(length = 20)
    private String phone;


    //@Builder.Default is a Lombok annotation that makes a field's default value work correctly when using
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    // unique id returned by Google/Facebook — used to match returning social-login users
    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // false until OTP (or social provider) confirms the account
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    // --- login lockout tracking ---
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    // how many times this account has already been locked; drives the escalating
    // lockout duration (1min -> 5min -> 1h -> 24h) in LockoutPolicy
    @Column(name = "lock_stage", nullable = false)
    @Builder.Default
    private int lockStage = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    // --- end login lockout tracking ---

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "customer")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "customer")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Review> reviews = new HashSet<>();
}