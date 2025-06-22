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
			// 直接使用 record 的建構子
			return new FunctionGroupDto(g.getId(), g.getName());
		}).collect(Collectors.toList());
	}
}