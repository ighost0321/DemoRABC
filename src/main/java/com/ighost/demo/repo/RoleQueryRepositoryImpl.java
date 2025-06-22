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

/**
 * RoleQueryRepository 的實作類別。 Spring Data JPA 會自動找到這個實作並與主 Repository 介面整合。
 */
@Repository
public class RoleQueryRepositoryImpl implements RoleQueryRepository {

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<Role> findByKeyword(String keyword, int offset, int pageSize) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Role> cq = cb.createQuery(Role.class);
		Root<Role> role = cq.from(Role.class);

		Predicate predicate = createKeywordPredicate(cb, role, keyword);
		if (predicate != null) {
			cq.where(predicate);
		}

		cq.select(role).distinct(true).orderBy(cb.asc(role.get("id")));

		TypedQuery<Role> query = em.createQuery(cq);
		query.setFirstResult(offset);
		query.setMaxResults(pageSize);

		return query.getResultList();
	}

	@Override
	public long countByKeyword(String keyword) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Role> role = cq.from(Role.class);

		Predicate predicate = createKeywordPredicate(cb, role, keyword);
		if (predicate != null) {
			cq.where(predicate);
		}

		cq.select(cb.countDistinct(role));

		return em.createQuery(cq).getSingleResult();
	}

	private Predicate createKeywordPredicate(CriteriaBuilder cb, Root<Role> role, String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}

		String kwPattern = "%" + keyword.toLowerCase() + "%";

		Join<Role, Function> functionJoin = role.join("functions", JoinType.LEFT);
		Join<Function, FunctionGroup> groupJoin = functionJoin.join("group", JoinType.LEFT);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.like(cb.lower(role.get("id")), kwPattern));
		predicates.add(cb.like(cb.lower(role.get("name")), kwPattern));
		predicates.add(cb.like(cb.lower(functionJoin.get("name")), kwPattern));
		predicates.add(cb.like(cb.lower(functionJoin.get("code")), kwPattern));
		predicates.add(cb.like(cb.lower(groupJoin.get("name")), kwPattern));

		return cb.or(predicates.toArray(new Predicate[0]));
	}
}
