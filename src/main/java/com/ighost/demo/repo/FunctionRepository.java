package com.ighost.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ighost.demo.entity.Function;

public interface FunctionRepository extends JpaRepository<Function, Integer> {

    List<Function> findByGroup_Id(Integer groupId);

    List<Function> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);

    List<Function> findAllByIdIn(List<Integer> ids);

    boolean existsByCodeIgnoreCase(String code);
}
