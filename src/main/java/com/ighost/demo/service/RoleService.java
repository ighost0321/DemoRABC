package com.ighost.demo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ighost.demo.entity.Function;
import com.ighost.demo.entity.Role;
import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.model.RoleDto;
import com.ighost.demo.repo.FunctionRepository;
import com.ighost.demo.repo.RoleRepository;

@Service
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private FunctionRepository functionRepository;

	@Transactional(readOnly = true)
	public List<RoleDto> findByKeyword(String keyword, int page, int pageSize) {
		int offset = (page - 1) * pageSize;
		List<Role> roles = roleRepository.findByKeyword(keyword, offset, pageSize);
		return roles.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public long countByKeyword(String keyword) {
		return roleRepository.countByKeyword(keyword);
	}

	/**
	 * 根據 ID 查詢單一角色。 回傳 Optional<RoleDto>，這是一種更現代、更安全的 Java 實踐， 能明確向上層呼叫者表示「值可能不存在」。
	 * * @param roleId 角色 ID
	 * 
	 * @return 包含 RoleDto 的 Optional，如果找不到則為空。
	 */
	@Transactional(readOnly = true)
	public Optional<RoleDto> findRoleById(String roleId) {
		return roleRepository.findById(roleId).map(this::convertToDto);
	}

	/**
	 * 儲存角色 (新增或更新)。
	 */
	@Transactional
	public void saveRole(String roleId, String roleName, List<Integer> functionIds) {
		Role role = roleRepository.findById(roleId).orElse(new Role());

		role.setId(roleId);
		role.setName(roleName);

		if (functionIds != null && !functionIds.isEmpty()) {
			List<Function> functions = functionRepository.findAllByIdIn(functionIds);
			role.setFunctions(new HashSet<>(functions));
		} else {
			if (role.getFunctions() != null) {
				role.getFunctions().clear();
			} else {
				role.setFunctions(new HashSet<>());
			}
		}

		roleRepository.save(role);
	}

	/**
	 * 將 Role 實體轉換為 RoleDto Record 的輔助方法。
	 */
	private RoleDto convertToDto(Role role) {
		List<FunctionDto> functionDtos = null;
		if (role.getFunctions() != null) {
			functionDtos = role.getFunctions().stream().map(f -> {
				String groupName = (f.getGroup() != null) ? f.getGroup().getName() : null;
				// 使用 FunctionDto 的 record 建構子
				return new FunctionDto(f.getId(), f.getCode(), f.getName(), f.getUrl(), f.getGroup().getId(),
						groupName);
			}).collect(Collectors.toList());
		}
		// 使用 RoleDto 的 record 建構子
		return new RoleDto(role.getId(), role.getName(), functionDtos);
	}
}