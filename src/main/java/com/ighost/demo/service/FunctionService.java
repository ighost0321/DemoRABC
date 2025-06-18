package com.ighost.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ighost.demo.entity.Function;
import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.repo.FunctionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FunctionService {

    private final FunctionRepository functionRepository;

    public List<FunctionDto> searchFunctions(String keyword) {
        List<Function> functions = functionRepository.findByNameContainingOrCodeContaining(keyword, keyword);
        return functions.stream().map(f -> {
            FunctionDto dto = new FunctionDto();
            dto.setId(f.getId());
            dto.setCode(f.getCode());
            dto.setName(f.getName());
            dto.setUrl(f.getUrl());
            dto.setGroupId(f.getGroup().getId());
            dto.setGroupName(f.getGroup().getName());
            return dto;
        }).collect(Collectors.toList());
    }
}
