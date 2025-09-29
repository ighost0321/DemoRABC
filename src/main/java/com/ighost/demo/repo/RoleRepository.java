package com.ighost.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ighost.demo.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>, RoleQueryRepository {

    @Override
    @EntityGraph(attributePaths = {"functions", "functions.group"})
    Optional<Role> findById(String id);
}
