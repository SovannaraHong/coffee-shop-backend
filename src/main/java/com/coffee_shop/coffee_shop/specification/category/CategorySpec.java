package com.coffee_shop.coffee_shop.specification.category;

import com.coffee_shop.coffee_shop.entity.Category;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record CategorySpec(CategoryFilter filter) implements Specification<Category> {
    //cb like condition in sql  like where cb.equal(...) |=
    //Represents the whole SQL query.SELECT *
    //FROM category
    //WHERE ...
    //ORDER BY ...
    //Root<Category> cate    cate.get("id") | category.id
    @Override
    public @Nullable Predicate toPredicate(Root<Category> cate, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(filter.getId())) {
            predicates.add(cb.equal(cate.get("id"), filter.getId()));
        }
        if (Objects.nonNull(filter.getName())) {
            predicates.add(cb.equal(cate.get("name"), "%" + filter.getName() + "%"));
        }
        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
