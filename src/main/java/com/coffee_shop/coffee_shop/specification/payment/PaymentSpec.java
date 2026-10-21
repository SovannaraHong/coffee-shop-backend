package com.coffee_shop.coffee_shop.specification.payment;

import com.coffee_shop.coffee_shop.entity.Payment;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentSpec implements Specification<Payment> {

    private final PaymentFilter filter;

    public PaymentSpec(PaymentFilter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<Payment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getId() != null) {
            predicates.add(cb.equal(root.get("id"), filter.getId()));
        }
        if (filter.getOrderId() != null) {
            predicates.add(cb.equal(root.get("order").get("id"), filter.getOrderId()));
        }
        if (filter.getMethod() != null) {
            predicates.add(cb.equal(root.get("method"), filter.getMethod()));
        }
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getTransactionRef() != null && !filter.getTransactionRef().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("transactionRef")),
                    "%" + filter.getTransactionRef().toLowerCase() + "%"));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}