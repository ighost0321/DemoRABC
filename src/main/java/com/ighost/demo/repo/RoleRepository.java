package com.ighost.demo.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.model.RoleDto;

@Repository
public class RoleRepository {
    @Autowired
    private NamedParameterJdbcTemplate namedJdbc;

    // 取得分頁的角色（含關鍵字）
    public List<RoleDto> findByKeyword(String keyword, int offset, int pageSize) {
        String baseSql = """
            SELECT r.id as role_id, r.name as role_name,
                   f.id as func_id, f.name as func_name,
                   f.code, fg.name as group_name, f.url
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
            ORDER BY r.id, f.id
            LIMIT :pageSize OFFSET :offset
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("kw", keyword == null ? "" : keyword.trim().toLowerCase());
        params.put("kwlike", "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%");
        params.put("pageSize", pageSize);
        params.put("offset", offset);

        List<Map<String, Object>> rows = namedJdbc.queryForList(baseSql, params);

        // 將查詢結果 group by 角色
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

    // 取得符合條件的總角色數
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
}
