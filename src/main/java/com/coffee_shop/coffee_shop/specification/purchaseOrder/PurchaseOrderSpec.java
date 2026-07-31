package com.coffee_shop.coffee_shop.specification.purchaseOrder;

import com.coffee_shop.coffee_shop.entity.PurchaseOrder;
import com.coffee_shop.coffee_shop.util.enums.PurchaseOrderStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record PurchaseOrderSpec(PurchaseOrderFilter filter) implements Specification<PurchaseOrder> {
    @Override
    public @Nullable Predicate toPredicate(Root<PurchaseOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filter.getId())) {
            predicates.add(cb.equal(root.get("id"), filter.getId()));
        }
        if (Objects.nonNull(filter.getSupplierId())) {
            predicates.add(cb.equal(root.get("supplier").get("id"), filter.getSupplierId()));
        }
        if (Objects.nonNull(filter.getStatus())) {
            predicates.add(cb.equal(root.get("status"), PurchaseOrderStatus.valueOf(filter.getStatus())));
        }
        if (Objects.nonNull(filter.getStartDate())) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getStartDate()));
        }
        if (Objects.nonNull(filter.getEndDate())) {
            predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), filter.getEndDate()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}