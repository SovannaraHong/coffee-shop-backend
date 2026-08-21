package com.coffee_shop.coffee_shop.entity;


import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "permission", uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_name", columnNames = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;
}
