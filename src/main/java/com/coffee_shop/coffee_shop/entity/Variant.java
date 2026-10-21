package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Variant extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_variant_product")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    // Small, Medium, Large
    @Column(nullable = false, length = 100)
    private String name;


    // Selling price
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(unique = true, length = 50)
    private String sku;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variant other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}