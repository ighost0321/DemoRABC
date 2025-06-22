package com.ighost.demo.controller;

import java.security.Principal; // 1. 匯入 Principal
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ighost.demo.model.FunctionDto; // 2. 匯入 FunctionDto
import com.ighost.demo.model.RoleDto;
import com.ighost.demo.service.RoleService;
import com.ighost.demo.service.UserService; // 3. 匯入 UserService

@Controller
public class RoleQueryController {

	@Autowired
	private RoleService roleService;

	// 4. 注入 UserService
	@Autowired
	private UserService userService;

	@Value("${custom.pagination.page-size}")
	private int pageSize;

	@GetMapping("/role-query")
	public String queryRoles(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "1") int page, Model model, Principal principal) { // 5. 新增
																											// Principal
																											// 參數

		int totalRoles = roleService.countByKeyword(keyword);
		int totalPages = (int) Math.ceil(totalRoles / (double) pageSize);

		if (page < 1)
			page = 1;
		if (page > totalPages && totalPages > 0)
			page = totalPages;

		List<RoleDto> roles = roleService.findByKeyword(keyword, page, pageSize);

		model.addAttribute("roles", roles);
		model.addAttribute("keyword", keyword);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("pageSize", pageSize);

		// 6. 取得側邊欄所需的功能和群組資料，並加入到 model 中
		List<FunctionDto> functions = userService.getFunctionsByUsername(principal.getName());
		List<String> groups = userService.getDistinctGroupsByUsername(principal.getName());
		model.addAttribute("groups", groups);
		model.addAttribute("functions", functions);

		return "role-query";
	}
}