package com.coffee_shop.coffee_shop.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "review",
        uniqueConstraints = {
                // one review per customer per product
                @UniqueConstraint(name = "uk_review_customer_product", columnNames = {"customer_id", "product_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends CreatedAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_customer")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_product")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    // valid range (1-5) enforced in ReviewCreateRequest via @Min/@Max, not at DB level
    @Column(nullable = false)
    private Integer rating;

    @Lob
    @Column(name = "review_comment")
    private String comment;
}