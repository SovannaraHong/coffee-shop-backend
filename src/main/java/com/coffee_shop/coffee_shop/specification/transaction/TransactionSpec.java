package com.coffee_shop.coffee_shop.specification.transaction;

import com.coffee_shop.coffee_shop.entity.InventoryTransaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record TransactionSpec(TransactionFilter filter) implements Specification<InventoryTransaction> {
    @Override
    public @Nullable Predicate toPredicate(Root<InventoryTransaction> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filter.getId())) {
            predicates.add(cb.equal(root.get("id"), filter.getId()));
        }
        if (Objects.nonNull(filter.getReferenceType())) {
            predicates.add(cb.equal(root.get("referenceType"), filter.getReferenceType()));
        }
        if (Objects.nonNull(filter.getReferenceId())) {
            predicates.add(cb.equal(root.get("referenceId"), filter.getReferenceId()));
        }
        if (Objects.nonNull(filter.getStartDate())) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
        }
        if (Objects.nonNull(filter.getEndDate())) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
        }

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}