package com.coffee_shop.coffee_shop.specification.supplier;

import com.coffee_shop.coffee_shop.entity.Supplier;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SupplierSpec(SupplierFilter filter) implements Specification<Supplier> {

    @Override
    public @Nullable Predicate toPredicate(Root<Supplier> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filter.getIsActive())) {
            predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
        }

        if (Objects.nonNull(filter.getKeyword()) && !filter.getKeyword().isBlank()) {
            String like = "%" + filter.getKeyword().toLowerCase() + "%";
            Predicate nameMatch = cb.like(cb.lower(root.get("name")), like);
            Predicate contactMatch = cb.like(cb.lower(root.get("contactPerson")), like);
            Predicate emailMatch = cb.like(cb.lower(root.get("email")), like);
            predicates.add(cb.or(nameMatch, contactMatch, emailMatch));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}