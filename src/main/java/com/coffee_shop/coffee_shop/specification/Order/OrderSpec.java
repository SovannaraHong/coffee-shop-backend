package com.coffee_shop.coffee_shop.specification.Order;

import com.coffee_shop.coffee_shop.entity.Order;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record OrderSpec(OrderFilter filter) implements Specification<Order> {

    @Override
    public @Nullable Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filter.getCustomerId())) {
            predicates.add(cb.equal(root.get("customer").get("id"), filter.getCustomerId()));
        }
        if (Objects.nonNull(filter.getStatus())) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }
        if (Objects.nonNull(filter.getFromDate())) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
        }
        if (Objects.nonNull(filter.getToDate())) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}