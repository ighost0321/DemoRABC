package com.ighost.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ighost.demo.model.FunctionDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class UserService {
	@PersistenceContext
	private EntityManager em;

	public List<FunctionDto> getFunctionsByUsername(String username) {
		String sql = "SELECT DISTINCT f.id, f.code, f.name, f.url, f.group_id, fg.name AS group_name " + "FROM users u "
				+ "JOIN user_roles ur ON u.id = ur.user_id " + "JOIN roles r ON ur.role_id = r.id "
				+ "JOIN role_functions rf ON r.id = rf.role_id " + "JOIN functions f ON rf.function_id = f.id "
				+ "JOIN function_group fg ON f.group_id = fg.id " + "WHERE u.username = :username";

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = em.createNativeQuery(sql).setParameter("username", username).getResultList();

		List<FunctionDto> dtos = new ArrayList<>();
		for (Object[] row : resultList) {
			// 使用 record 建構子來建立 FunctionDto
			FunctionDto dto = new FunctionDto(row[0] == null ? null : ((Number) row[0]).intValue(), (String) row[1],
					(String) row[2], (String) row[3], row[4] == null ? null : ((Number) row[4]).intValue(),
					(String) row[5]);
			dtos.add(dto);
		}
		return dtos;
	}
}