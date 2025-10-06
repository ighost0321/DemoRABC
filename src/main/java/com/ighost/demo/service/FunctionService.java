package com.ighost.demo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.ighost.demo.entity.Function;
import com.ighost.demo.entity.FunctionGroup;
import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.repo.FunctionGroupRepository;
import com.ighost.demo.repo.FunctionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FunctionService {

    private final FunctionRepository functionRepository;
    private final FunctionGroupRepository functionGroupRepository;

    private static final Comparator<FunctionDto> FUNCTION_GROUP_ORDER = Comparator
            .comparing(FunctionDto::groupId, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(FunctionDto::id, Comparator.nullsLast(Integer::compareTo));

    public List<FunctionDto> searchFunctions(Integer functionId, String keyword) {
        boolean hasKeyword = StringUtils.hasText(keyword);

        if (functionId != null) {
            return functionRepository.findById(functionId)
                    .stream()
                    .filter(function -> !hasKeyword || matchesKeyword(function, keyword))
                    .map(this::convertToDto)
                    .sorted(FUNCTION_GROUP_ORDER)
                    .toList();
        }

        if (!hasKeyword) {
            return findAllFunctions();
        }

        String trimmedKeyword = keyword.trim();
        return functionRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(trimmedKeyword, trimmedKeyword).stream()
                .map(this::convertToDto)
                .sorted(FUNCTION_GROUP_ORDER)
                .toList();
    }

    public List<FunctionDto> findAllFunctions() {
        return functionRepository.findAll().stream()
                .map(this::convertToDto)
                .sorted(FUNCTION_GROUP_ORDER)
                .toList();
    }

    @Transactional
    public void updateFunction(Integer id, Integer groupId, String code, String name, String url) {
        if (id == null) {
            throw new IllegalArgumentException("缺少功能代碼，無法更新。");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("請選擇功能群組。");
        }

        Function function = functionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到指定的功能。"));
        FunctionGroup group = functionGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("找不到功能群組。"));

        function.setGroup(group);
        function.setCode(requireText(code, "功能英文名稱不可為空白。"));
        function.setName(requireText(name, "功能中文名稱不可為空白。"));
        function.setUrl(requireText(url, "功能路徑不可為空白。"));
    }

    @Transactional
    public FunctionDto createFunction(Integer groupId, String code, String name, String url) {
        if (groupId == null) {
            throw new IllegalArgumentException("請選擇功能群組。");
        }

        String sanitizedCode = requireText(code, "功能英文名稱不可為空白。");
        if (functionRepository.existsByCodeIgnoreCase(sanitizedCode)) {
            throw new IllegalArgumentException("功能英文名稱已存在，請重新輸入。");
        }

        FunctionGroup group = functionGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("找不到功能群組。"));

        Function function = new Function();
        function.setGroup(group);
        function.setCode(sanitizedCode);
        function.setName(requireText(name, "功能中文名稱不可為空白。"));
        function.setUrl(requireText(url, "功能路徑不可為空白。"));

        Function savedFunction = functionRepository.save(function);
        return convertToDto(savedFunction);
    }

    private boolean matchesKeyword(Function function, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

        String trimmedKeyword = keyword.trim();
        String keywordLower = trimmedKeyword.toLowerCase(Locale.ROOT);

        return containsIgnoreCase(function.getName(), keywordLower)
                || containsIgnoreCase(function.getCode(), keywordLower);
    }

    private boolean containsIgnoreCase(String value, String keywordLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keywordLower);
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private FunctionDto convertToDto(Function function) {
        Integer groupId = function.getGroup() != null ? function.getGroup().getId() : null;
        String groupName = function.getGroup() != null ? function.getGroup().getName() : null;
        return new FunctionDto(function.getId(), function.getCode(), function.getName(), function.getUrl(), groupId, groupName);
    }
}
