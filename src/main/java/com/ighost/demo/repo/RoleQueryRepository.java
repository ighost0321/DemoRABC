package com.ighost.demo.repo;

import java.util.List;

import com.ighost.demo.entity.Role;

public interface RoleQueryRepository {

    List<Role> findByKeyword(String keyword, int offset, int pageSize);

    long countByKeyword(String keyword);
}
