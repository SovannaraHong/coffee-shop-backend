package com.coffee_shop.coffee_shop.specification.variant;

import com.coffee_shop.coffee_shop.entity.Variant;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record VariantSpec(VariantFilter variantFilter) implements Specification<Variant> {
    @Override
    public @Nullable Predicate toPredicate(Root<Variant> variant, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(variantFilter.getId())) {
            predicates.add(cb.equal(variant.get("id"), variantFilter.getId()));
        }
        if (Objects.nonNull(variantFilter.getName())) {
            predicates.add(cb.like(cb.lower(variant.get("name")), "%" + variantFilter.getName().toLowerCase() + "%"));
        }
        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
