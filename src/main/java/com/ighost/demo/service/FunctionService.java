package com.ighost.demo.service;

import java.util.List;

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
        return functions.stream().map(this::convertToDto).toList();
    }

    public List<FunctionDto> findAllFunctions() {
        return functionRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    private FunctionDto convertToDto(Function function) {
        Integer groupId = function.getGroup() != null ? function.getGroup().getId() : null;
        String groupName = function.getGroup() != null ? function.getGroup().getName() : null;
        return new FunctionDto(function.getId(), function.getCode(), function.getName(), function.getUrl(), groupId, groupName);
    }
}
