package com.ighost.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ighost.demo.model.RoleDto;
import com.ighost.demo.service.RoleService;

@Controller
public class RoleQueryController {
	@Autowired
	private RoleService roleService;

	@GetMapping("/role-query")
	public String queryRoles(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page, Model model) {

		int pageSize = 5;
		int totalRoles = roleService.countByKeyword(keyword);
		int totalPages = (int) Math.ceil(totalRoles / (double) pageSize);

		// 確保頁碼在範圍內
		if (page < 1)
			page = 1;
		if (page > totalPages && totalPages > 0)
			page = totalPages;

		List<RoleDto> roles = roleService.findByKeyword(keyword, page, pageSize);

		model.addAttribute("roles", roles);
		model.addAttribute("keyword", keyword);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);

		// 你如果還有 sidebar 參數
		// model.addAttribute("groups", ...);
		// model.addAttribute("functions", ...);

		return "role-query"; // 對應 role-query.html Thymeleaf 頁面
	}
}
