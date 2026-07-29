package com.coffee_shop.coffee_shop.specification.stockAdjustment;

import com.coffee_shop.coffee_shop.entity.StockAdjustment;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record StockAdjustmentSpec(StockAdjustmentFilter filter) implements Specification<StockAdjustment> {

    @Override
    public @Nullable Predicate toPredicate(Root<StockAdjustment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filter.getIngredientId())) {
            predicates.add(cb.equal(root.get("ingredient").get("id"), filter.getIngredientId()));
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