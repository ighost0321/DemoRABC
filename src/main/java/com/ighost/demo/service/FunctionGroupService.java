package com.ighost.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ighost.demo.model.FunctionGroupDto;
import com.ighost.demo.repo.FunctionGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FunctionGroupService {

    private final FunctionGroupRepository functionGroupRepository;

    public List<FunctionGroupDto> getAllGroups() {
        return functionGroupRepository.findAll().stream().map(g -> {
            FunctionGroupDto dto = new FunctionGroupDto();
            dto.setId(g.getId());
            dto.setName(g.getName());
            return dto;
        }).collect(Collectors.toList());
    }
}
