package com.ighost.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.model.RoleDto;
import com.ighost.demo.service.RoleService;
import com.ighost.demo.service.UserService;

/**
 * 處理「角色查詢」頁面的請求。
 */
@Controller
public class RoleQueryController {

    private final RoleService roleService;
    private final UserService userService;
    private final int pageSize;

    public RoleQueryController(RoleService roleService,
            UserService userService,
            @Value("${custom.pagination.page-size}") int pageSize) {
        this.roleService = roleService;
        this.userService = userService;
        this.pageSize = pageSize;
    }

    @GetMapping("/role-query")
    public String queryRoles(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model,
            Principal principal) {

        long totalRoles = roleService.countByKeyword(keyword);
        int totalPages = calculateTotalPages(totalRoles);
        int currentPage = clampPage(page, totalPages);

        List<RoleDto> roles = roleService.findByKeyword(keyword, currentPage, pageSize);

        model.addAttribute("roles", roles);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);

        List<FunctionDto> userFunctions = userService.getFunctionsByUsername(principal.getName());
        boolean canEditRole = userFunctions.stream()
                .map(FunctionDto::url)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .anyMatch("/role-edit"::equalsIgnoreCase);
        model.addAttribute("canEditRole", canEditRole);
        model.addAttribute("functions", userFunctions);
        model.addAttribute("groups", extractGroups(userFunctions));

        return "role-query";
    }

    private int calculateTotalPages(long totalRoles) {
        if (totalRoles <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalRoles / pageSize);
    }

    private int clampPage(int requestedPage, int totalPages) {
        int sanitizedPage = Math.max(requestedPage, 1);
        if (totalPages > 0) {
            sanitizedPage = Math.min(sanitizedPage, totalPages);
        }
        return sanitizedPage;
    }

    private List<String> extractGroups(List<FunctionDto> functions) {
        return functions.stream()
                .map(FunctionDto::groupName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }
}
