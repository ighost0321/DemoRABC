package com.ighost.demo.repo;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ighost.demo.entity.Function;

public interface FunctionGroupRepository extends JpaRepository<Function, Integer> {
    List<Function> findByGroup_Id(Integer groupId); // 注意這裡是 group 不是 functionGroup
}


