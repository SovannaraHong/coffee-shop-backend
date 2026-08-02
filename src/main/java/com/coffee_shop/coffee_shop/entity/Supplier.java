package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "supplier",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_supplier_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_supplier_phone", columnNames = "phone")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "supplier")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PurchaseOrder> purchaseOrders = new HashSet<>();
}