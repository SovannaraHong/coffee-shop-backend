package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ingredient")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    // kg, g, l, ml, piece...
    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "quantity_in_stock", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal quantityInStock = BigDecimal.ZERO;

    @Column(name = "reorder_level", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    @OneToMany(mappedBy = "ingredient")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<RecipeIngredient> recipeIngredients = new HashSet<>();
}