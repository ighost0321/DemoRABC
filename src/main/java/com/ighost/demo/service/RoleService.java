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

    public List<RoleDto> findByRoleOrFunction(String keyword) {
        return roleRepository.findByKeyword(keyword == null || keyword.trim().isEmpty() ? null : keyword.trim());
    }
}

