package com.coffee_shop.coffee_shop.specification.auditLog;

import com.coffee_shop.coffee_shop.entity.AuditLog;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AuditLogSpec(AuditLogFilter filter) implements Specification<AuditLog> {

    @Override
    public @Nullable Predicate toPredicate(Root<AuditLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filter.getEmail())) {
            predicates.add(cb.equal(cb.lower(root.get("email")), filter.getEmail().toLowerCase()));
        }
        if (Objects.nonNull(filter.getEventType())) {
            predicates.add(cb.equal(root.get("eventType"), filter.getEventType()));
        }
        if (Objects.nonNull(filter.getSuccess())) {
            predicates.add(cb.equal(root.get("success"), filter.getSuccess()));
        }
        if (Objects.nonNull(filter.getIpAddress())) {
            predicates.add(cb.equal(root.get("ipAddress"), filter.getIpAddress()));
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