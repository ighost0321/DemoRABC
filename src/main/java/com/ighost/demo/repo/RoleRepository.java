package com.ighost.demo.repo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.model.RoleDto;

@Repository
public class RoleRepository {
	@Autowired
	private NamedParameterJdbcTemplate namedJdbc;

	// 取得分頁的角色（含關鍵字）
	public List<RoleDto> findByKeyword(String keyword, int offset, int pageSize) {

		// --- 第一步：先找出當前頁面應該顯示的角色 ID ---
		String roleIdSql = """
				    SELECT DISTINCT r.id
				    FROM roles r
				    LEFT JOIN role_functions rf ON r.id = rf.role_id
				    LEFT JOIN functions f ON rf.function_id = f.id
				    LEFT JOIN function_group fg ON f.group_id = fg.id
				    WHERE (
				        :kw IS NULL OR :kw = '' OR
				        LOWER(r.name) LIKE :kwlike OR
				        LOWER(r.id) LIKE :kwlike OR
				        LOWER(f.name) LIKE :kwlike OR
				        LOWER(f.code) LIKE :kwlike OR
				        LOWER(fg.name) LIKE :kwlike
				    )
				    ORDER BY r.id
				    LIMIT :pageSize OFFSET :offset
				""";

		Map<String, Object> params = new HashMap<>();
		params.put("kw", keyword == null ? "" : keyword.trim().toLowerCase());
		params.put("kwlike", "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%");
		params.put("pageSize", pageSize);
		params.put("offset", offset);

		List<String> roleIds = namedJdbc.queryForList(roleIdSql, params, String.class);

		// 如果在該分頁上找不到任何角色 ID，直接回傳空列表，避免後續查詢出錯
		if (roleIds.isEmpty()) {
			return Collections.emptyList();
		}

		// --- 第二步：根據上面得到的角色 ID，查詢這些角色的完整資訊 ---
		String finalSql = """
				    SELECT r.id as role_id, r.name as role_name,
				           f.id as func_id, f.name as func_name,
				           f.code, fg.name as group_name, f.url
				    FROM roles r
				    LEFT JOIN role_functions rf ON r.id = rf.role_id
				    LEFT JOIN functions f ON rf.function_id = f.id
				    LEFT JOIN function_group fg ON f.group_id = fg.id
				    WHERE r.id IN (:roleIds)
				    ORDER BY r.id, f.id
				""";

		Map<String, Object> finalParams = new HashMap<>();
		finalParams.put("roleIds", roleIds);

		List<Map<String, Object>> rows = namedJdbc.queryForList(finalSql, finalParams);

		// 將查詢結果 group by 角色的邏輯維持不變
		Map<String, RoleDto> roleMap = new LinkedHashMap<>();
		for (Map<String, Object> row : rows) {
			String rid = (String) row.get("role_id");
			RoleDto role = roleMap.computeIfAbsent(rid, id -> {
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
		return new ArrayList<>(roleMap.values());
	}

	// `countByKeyword` 的邏輯是正確的，計算的是不重複的角色總數，因此不需要修改
	public int countByKeyword(String keyword) {
		String countSql = """
				    SELECT COUNT(DISTINCT r.id)
				    FROM roles r
				    LEFT JOIN role_functions rf ON r.id = rf.role_id
				    LEFT JOIN functions f ON rf.function_id = f.id
				    LEFT JOIN function_group fg ON f.group_id = fg.id
				    WHERE (
				        :kw IS NULL OR :kw = '' OR
				        LOWER(r.name) LIKE :kwlike OR
				        LOWER(r.id) LIKE :kwlike OR
				        LOWER(f.name) LIKE :kwlike OR
				        LOWER(f.code) LIKE :kwlike OR
				        LOWER(fg.name) LIKE :kwlike
				    )
				""";

		Map<String, Object> params = new HashMap<>();
		params.put("kw", keyword == null ? "" : keyword.trim().toLowerCase());
		params.put("kwlike", "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%");
		Integer count = namedJdbc.queryForObject(countSql, params, Integer.class);
		return count != null ? count : 0;
	}

	/**
	 * 根據角色 ID 查找單一角色及其對應的所有功能
	 * 
	 * @param roleId 角色 ID
	 * @return 包含完整功能列表的 RoleDto
	 */
	public Optional<RoleDto> findById(String roleId) {
		String sql = """
				    SELECT r.id as role_id, r.name as role_name,
				           f.id as func_id, f.name as func_name,
				           f.code, fg.name as group_name, f.url
				    FROM roles r
				    LEFT JOIN role_functions rf ON r.id = rf.role_id
				    LEFT JOIN functions f ON rf.function_id = f.id
				    LEFT JOIN function_group fg ON f.group_id = fg.id
				    WHERE r.id = :roleId
				    ORDER BY f.id
				""";

		Map<String, Object> params = new HashMap<>();
		params.put("roleId", roleId);

		List<Map<String, Object>> rows = namedJdbc.queryForList(sql, params);

		if (rows.isEmpty()) {
			return Optional.empty();
		}

		// 使用與 findByKeyword 相同的邏輯重組資料
		RoleDto role = new RoleDto();
		role.setId((String) rows.get(0).get("role_id"));
		role.setName((String) rows.get(0).get("role_name"));
		role.setFunctions(new ArrayList<>());

		for (Map<String, Object> row : rows) {
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
		return Optional.of(role);
	}

	/**
	 * 儲存或更新一個角色及其權限。 使用 @Transactional 確保資料操作的原子性。
	 * 
	 * @param roleId      角色 ID
	 * @param roleName    角色名稱
	 * @param functionIds 該角色擁有的功能 ID 列表
	 */
	@Transactional
	public void save(String roleId, String roleName, List<Integer> functionIds) {
		// 1. 新增或更新 roles 表
		String upsertRoleSql = """
				    INSERT INTO roles (id, name)
				    VALUES (:id, :name)
				    ON CONFLICT (id) DO UPDATE SET name = :name
				""";
		Map<String, Object> roleParams = new HashMap<>();
		roleParams.put("id", roleId);
		roleParams.put("name", roleName);
		namedJdbc.update(upsertRoleSql, roleParams);

		// 2. 刪除該角色在 role_functions 中所有舊的關聯
		String deleteFunctionsSql = "DELETE FROM role_functions WHERE role_id = :roleId";
		Map<String, Object> deleteParams = Collections.singletonMap("roleId", roleId);
		namedJdbc.update(deleteFunctionsSql, deleteParams);

		// 3. 如果有新的權限，就批次插入新的關聯
		if (functionIds != null && !functionIds.isEmpty()) {
			String insertFunctionsSql = "INSERT INTO role_functions (role_id, function_id) VALUES (:roleId, :functionId)";
			Map<String, Object>[] batchValues = functionIds.stream().map(functionId -> {
				Map<String, Object> map = new HashMap<>();
				map.put("roleId", roleId);
				map.put("functionId", functionId);
				return map;
			}).toArray(Map[]::new);
			namedJdbc.batchUpdate(insertFunctionsSql, batchValues);
		}
	}
}