package com.ighost.demo.repo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.ighost.demo.entity.Function;
import com.ighost.demo.entity.FunctionGroup;
import com.ighost.demo.entity.Role;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class RoleQueryRepositoryImpl implements RoleQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Role> findByKeyword(String keyword, int offset, int pageSize) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Role> query = builder.createQuery(Role.class);
        Root<Role> role = query.from(Role.class);

        Predicate predicate = createKeywordPredicate(builder, role, keyword);
        if (predicate != null) {
            query.where(predicate);
        }

        query.select(role)
                .distinct(true)
                .orderBy(builder.asc(role.get("id")));

        TypedQuery<Role> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(pageSize);
        return typedQuery.getResultList();
    }

    @Override
    public long countByKeyword(String keyword) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Role> role = query.from(Role.class);

        Predicate predicate = createKeywordPredicate(builder, role, keyword);
        if (predicate != null) {
            query.where(predicate);
        }

        query.select(builder.countDistinct(role));
        return entityManager.createQuery(query).getSingleResult();
    }

    private Predicate createKeywordPredicate(CriteriaBuilder builder, Root<Role> role, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        String pattern = "%" + keyword.toLowerCase() + "%";

        Join<Role, Function> functionJoin = role.join("functions", JoinType.LEFT);
        Join<Function, FunctionGroup> groupJoin = functionJoin.join("group", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.like(builder.lower(role.get("id")), pattern));
        predicates.add(builder.like(builder.lower(role.get("name")), pattern));
        predicates.add(builder.like(builder.lower(functionJoin.get("name")), pattern));
        predicates.add(builder.like(builder.lower(functionJoin.get("code")), pattern));
        predicates.add(builder.like(builder.lower(groupJoin.get("name")), pattern));

        return builder.or(predicates.toArray(new Predicate[0]));
    }
}
