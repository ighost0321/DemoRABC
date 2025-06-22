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
		return functions.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	/**
	 * 查詢系統中所有的功能 * @return 所有功能的列表
	 */
	public List<FunctionDto> findAllFunctions() {
		List<Function> functions = functionRepository.findAll();
		return functions.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	/**
	 * 將 Function 實體轉換為 FunctionDto Record。
	 */
	private FunctionDto convertToDto(Function f) {
		Integer groupId = (f.getGroup() != null) ? f.getGroup().getId() : null;
		String groupName = (f.getGroup() != null) ? f.getGroup().getName() : null;
		return new FunctionDto(f.getId(), f.getCode(), f.getName(), f.getUrl(), groupId, groupName);
	}
}