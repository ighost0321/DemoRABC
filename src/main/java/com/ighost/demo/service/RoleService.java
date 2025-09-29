package com.ighost.demo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ighost.demo.entity.Function;
import com.ighost.demo.entity.Role;
import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.model.RoleDto;
import com.ighost.demo.repo.FunctionRepository;
import com.ighost.demo.repo.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final FunctionRepository functionRepository;

    @Transactional(readOnly = true)
    public List<RoleDto> findByKeyword(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return roleRepository.findByKeyword(keyword, offset, pageSize).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countByKeyword(String keyword) {
        return roleRepository.countByKeyword(keyword);
    }

    @Transactional(readOnly = true)
    public Optional<RoleDto> findRoleById(String roleId) {
        return roleRepository.findById(roleId).map(this::convertToDto);
    }

    @Transactional
    public void saveRole(String roleId, String roleName, List<Integer> functionIds) {
        Role role = roleRepository.findById(roleId).orElseGet(Role::new);

        role.setId(roleId);
        role.setName(roleName);

        if (functionIds != null && !functionIds.isEmpty()) {
            List<Function> functions = functionRepository.findAllByIdIn(functionIds);
            role.setFunctions(new HashSet<>(functions));
        } else if (role.getFunctions() != null) {
            role.getFunctions().clear();
        } else {
            role.setFunctions(new HashSet<>());
        }

        roleRepository.save(role);
    }

    private RoleDto convertToDto(Role role) {
        List<FunctionDto> functionDtos = null;
        if (role.getFunctions() != null) {
            functionDtos = role.getFunctions().stream()
                    .map(function -> new FunctionDto(
                            function.getId(),
                            function.getCode(),
                            function.getName(),
                            function.getUrl(),
                            function.getGroup() != null ? function.getGroup().getId() : null,
                            function.getGroup() != null ? function.getGroup().getName() : null))
                    .toList();
        }
        return new RoleDto(role.getId(), role.getName(), functionDtos);
    }
}
