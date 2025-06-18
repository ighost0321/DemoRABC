package com.ighost.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ighost.demo.model.FunctionDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class UserService {
    @PersistenceContext
    private EntityManager em;

    public List<FunctionDto> getFunctionsByUsername(String username) {
        String sql = "SELECT DISTINCT f.id, f.code, f.name, f.url, f.group_id, fg.name AS group_name " +
                "FROM users u " +
                "JOIN user_roles ur ON u.id = ur.user_id " +
                "JOIN roles r ON ur.role_id = r.id " +
                "JOIN role_functions rf ON r.id = rf.role_id " +
                "JOIN functions f ON rf.function_id = f.id " +
                "JOIN function_group fg ON f.group_id = fg.id " +
                "WHERE u.username = :username";

        @SuppressWarnings("unchecked")
        List<Object[]> resultList = em.createNativeQuery(sql)
                .setParameter("username", username)
                .getResultList();

        List<FunctionDto> dtos = new ArrayList<>();
        for (Object[] row : resultList) {
            FunctionDto dto = new FunctionDto();
            dto.setId(row[0] == null ? null : ((Number) row[0]).intValue());
            dto.setCode((String) row[1]);
            dto.setName((String) row[2]);
            dto.setUrl((String) row[3]);
            dto.setGroupId(row[4] == null ? null : ((Number) row[4]).intValue());
            dto.setGroupName((String) row[5]);
            dtos.add(dto);
        }
        return dtos;
    }

    // 取得所有功能群組名稱（不重複）
    public List<String> getDistinctGroupsByUsername(String username) {
        List<FunctionDto> functions = getFunctionsByUsername(username);
        return functions.stream()
                .map(FunctionDto::getGroupName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
