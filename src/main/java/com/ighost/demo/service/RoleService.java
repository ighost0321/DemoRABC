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
	 * 根據 ID 查詢單一角色，並轉換為 DTO。 回傳型別改為 RoleDto，找不到則回傳 null，以避免 AOP 代理的泛型問題。
	 */
	@Transactional(readOnly = true)
	public RoleDto findRoleById(String roleId) {
		Optional<Role> roleOptional = roleRepository.findById(roleId);
		if (roleOptional.isPresent()) {
			return convertToDto(roleOptional.get());
		}
		return null; // 直接回傳 null
	}

	/**
	 * 儲存角色 (新增或更新)。
	 */
	@Transactional
	public void saveRole(String roleId, String roleName, List<Integer> functionIds) {
		// findById 回傳的是 Optional<Role>，我們需要先處理它
		Role role = roleRepository.findById(roleId).orElse(new Role());

		role.setId(roleId);
		role.setName(roleName);

		// 使用 HashSet 提高效率並確保唯一性
		if (functionIds != null && !functionIds.isEmpty()) {
			List<Function> functions = functionRepository.findAllByIdIn(functionIds);
			role.setFunctions(new HashSet<>(functions));
		} else {
			// 如果沒有傳入任何 functionIds，就清空現有的權限
			if (role.getFunctions() != null) {
				role.getFunctions().clear();
			}
		}

		roleRepository.save(role);
	}

	/**
	 * 將 Role 實體轉換為 RoleDto 的輔助方法。
	 */
	private RoleDto convertToDto(Role role) {
		RoleDto roleDto = new RoleDto();
		roleDto.setId(role.getId());
		roleDto.setName(role.getName());

		if (role.getFunctions() != null) {
			List<FunctionDto> functionDtos = role.getFunctions().stream().map(f -> {
				FunctionDto funcDto = new FunctionDto();
				funcDto.setId(f.getId());
				funcDto.setName(f.getName());
				funcDto.setCode(f.getCode());
				funcDto.setUrl(f.getUrl());
				if (f.getGroup() != null) {
					funcDto.setGroupName(f.getGroup().getName());
				}
				return funcDto;
			}).collect(Collectors.toList());
			roleDto.setFunctions(functionDtos);
		}

		return roleDto;
	}
}
