package com.ighost.demo.repo.spec;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.ighost.demo.entity.Function;
import com.ighost.demo.entity.FunctionGroup;
import com.ighost.demo.entity.Role;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public final class RoleSpecification {

    private RoleSpecification() {
    }

    public static Specification<Role> keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";

        return (root, query, builder) -> {
            query.distinct(true);

            Join<Role, Function> functionJoin = root.join("functions", JoinType.LEFT);
            Join<Function, FunctionGroup> groupJoin = functionJoin.join("group", JoinType.LEFT);

            return builder.or(
                    builder.like(builder.lower(root.get("id")), pattern),
                    builder.like(builder.lower(root.get("name")), pattern),
                    builder.like(builder.lower(functionJoin.get("name")), pattern),
                    builder.like(builder.lower(functionJoin.get("code")), pattern),
                    builder.like(builder.lower(groupJoin.get("name")), pattern));
        };
    }
}
