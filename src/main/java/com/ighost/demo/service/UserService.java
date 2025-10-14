package com.ighost.demo.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ighost.demo.model.FunctionDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String FUNCTIONS_BY_USER_SQL = """
            SELECT DISTINCT f.id,
                            f.code,
                            f.name,
                            f.url,
                            f.group_id,
                            fg.name AS group_name
            FROM users u
                 JOIN user_roles ur ON u.id = ur.user_id
                 JOIN roles r ON ur.role_id = r.id
                 JOIN role_functions rf ON r.id = rf.role_id
                 JOIN functions f ON rf.function_id = f.id
                 LEFT JOIN function_group fg ON f.group_id = fg.id
            WHERE u.username = :username
            ORDER BY f.group_id NULLS LAST, f.id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<FunctionDto> getFunctionsByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return List.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource("username", username.trim());
        return jdbcTemplate.query(FUNCTIONS_BY_USER_SQL, params, functionRowMapper());
    }

    private RowMapper<FunctionDto> functionRowMapper() {
        return (ResultSet rs, int rowNum) -> new FunctionDto(
                getInteger(rs, "id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("url"),
                getInteger(rs, "group_id"),
                rs.getString("group_name"));
    }

    private Integer getInteger(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        return rs.wasNull() ? null : value;
    }
}
