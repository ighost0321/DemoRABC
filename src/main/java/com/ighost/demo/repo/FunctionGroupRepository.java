package com.ighost.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ighost.demo.entity.FunctionGroup;

public interface FunctionGroupRepository extends JpaRepository<FunctionGroup, Integer> {

    @EntityGraph(attributePaths = "functions")
    List<FunctionGroup> findAll();
}
