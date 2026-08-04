package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "order_detail_addon")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailAddon extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_detail_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_detail_addon_detail")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "addon_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_detail_addon_addon")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Addon addon;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}