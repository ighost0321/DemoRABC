package com.ighost.demo.repo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.model.RoleDto;

@Repository
public class RoleRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<RoleDto> findByKeyword(String keyword) {
		String baseSql = """
				    SELECT r.id as role_id, r.name as role_name,
				           f.id as func_id, f.name as func_name,
				           f.code, fg.name as group_name, f.url
				    FROM roles r
				    LEFT JOIN role_functions rf ON r.id = rf.role_id
				    LEFT JOIN functions f ON rf.function_id = f.id
				    LEFT JOIN function_group fg ON f.group_id = fg.id
				""";

		List<Map<String, Object>> rows;
		Map<String, RoleDto> map = new LinkedHashMap<>();
		if (keyword == null || keyword.trim().isEmpty()) {
			// 查全部資料
			String sql = baseSql + " ORDER BY r.id, f.id";
			rows = jdbcTemplate.queryForList(sql);
		} else {
			// 查有關鍵字的
			String sql = baseSql + """
					    WHERE r.name ILIKE '%' || ? || '%'
					       OR r.id ILIKE '%' || ? || '%'
					       OR f.name ILIKE '%' || ? || '%'
					       OR f.code ILIKE '%' || ? || '%'
					       OR fg.name ILIKE '%' || ? || '%'
					    ORDER BY r.id, f.id
					""";
			Object[] args = new Object[] { keyword, keyword, keyword, keyword, keyword };
			rows = jdbcTemplate.queryForList(sql, args);
		}

		for (Map<String, Object> row : rows) {
			String rid = (String) row.get("role_id");
			RoleDto role = map.computeIfAbsent(rid, id -> {
				RoleDto dto = new RoleDto();
				dto.setId((String) row.get("role_id"));
				dto.setName((String) row.get("role_name"));
				dto.setFunctions(new ArrayList<>());
				return dto;
			});
			if (row.get("func_id") != null) {
				FunctionDto f = new FunctionDto();
				f.setId(row.get("func_id") != null ? ((Number) row.get("func_id")).intValue() : null);
				f.setName((String) row.get("func_name"));
				f.setCode((String) row.get("code"));
				f.setGroupName((String) row.get("group_name"));
				f.setUrl((String) row.get("url"));
				role.getFunctions().add(f);
			}
		}
		return new ArrayList<>(map.values());
	}

}
