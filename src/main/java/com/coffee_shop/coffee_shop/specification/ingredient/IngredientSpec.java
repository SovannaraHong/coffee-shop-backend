package com.coffee_shop.coffee_shop.specification.ingredient;

import com.coffee_shop.coffee_shop.entity.Ingredient;
import jakarta.persistence.criteria.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record IngredientSpec(IngredientFilter filter) implements Specification<Ingredient> {

    @Override
    public @Nullable Predicate toPredicate(Root<Ingredient> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(filter.getId())) {
            predicates.add(cb.equal(root.get("id"), filter.getId()));
        }

        if (StringUtils.hasText(filter.getName())) {
            predicates.add(likeIgnoreCase(cb, root.get("name"), filter.getName()));
        }

        if (StringUtils.hasText(filter.getUnit())) {
            predicates.add(likeIgnoreCase(cb, root.get("unit"), filter.getUnit()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate likeIgnoreCase(CriteriaBuilder cb, Expression<String> field, String value) {
        return cb.like(cb.lower(field), "%" + value.toLowerCase() + "%");
    }
}