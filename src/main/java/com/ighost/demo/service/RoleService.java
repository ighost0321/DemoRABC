package com.ighost.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ighost.demo.model.RoleDto;
import com.ighost.demo.repo.RoleRepository;

@Service
public class RoleService {
	@Autowired
	private RoleRepository roleRepository;

	public List<RoleDto> findByKeyword(String keyword, int page, int pageSize) {
		int offset = (page - 1) * pageSize;
		return roleRepository.findByKeyword(keyword, offset, pageSize);
	}

	public int countByKeyword(String keyword) {
		return roleRepository.countByKeyword(keyword);
	}
}
