package com.coffee_shop.coffee_shop.specification.product;

import com.coffee_shop.coffee_shop.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ProductSpec(ProductFilter filter) implements Specification<Product> {
    @Override
    public @Nullable Predicate toPredicate(Root<Product> product, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(filter.getId())) {
            predicates.add(cb.equal(product.get("id"), filter.getId()));
        }
        if (Objects.nonNull(filter.getName())) {
            predicates.add(cb.like(product.get("name"), "%" + filter.getName() + "%"));
        }
        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
