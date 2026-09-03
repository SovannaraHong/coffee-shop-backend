package com.coffee_shop.coffee_shop.specification.user;

import com.coffee_shop.coffee_shop.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record UserSpec(UserFilter filter) implements Specification<User> {
    @Override
    public @Nullable Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filter.getFullName())) {
            predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + filter.getFullName().toLowerCase() + "%"));
        }
        if (Objects.nonNull(filter.getEmail())) {
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + filter.getEmail().toLowerCase() + "%"));
        }
        if (Objects.nonNull(filter.getIsActive())) {
            predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
        }
        if (Objects.nonNull(filter.getRoleId())) {
            predicates.add(cb.equal(root.get("role").get("id"), filter.getRoleId()));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}