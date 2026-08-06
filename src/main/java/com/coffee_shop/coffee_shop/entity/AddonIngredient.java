package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "addon_ingredient")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddonIngredient extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "addon_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_addon_ingredient_addon")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Addon addon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ingredient_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_addon_ingredient_ingredient")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Ingredient ingredient;

    // amount of this ingredient consumed per 1 unit of the addon (e.g. 18g coffee beans per Extra Shot)
    @Column(name = "quantity_required", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityRequired;
}