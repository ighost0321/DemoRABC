package com.ighost.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ighost.demo.model.FunctionGroupDto;
import com.ighost.demo.repo.FunctionGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FunctionGroupService {

    private final FunctionGroupRepository functionGroupRepository;

    public List<FunctionGroupDto> getAllGroups() {
        return functionGroupRepository.findAll().stream()
                .map(group -> new FunctionGroupDto(group.getId(), group.getName()))
                .toList();
    }
}
