package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "app_user", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends CreatedAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;


    @Column(nullable = false, unique = true, length = 150)
    private String email;


    @Column(nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_role"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Role role;


    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
