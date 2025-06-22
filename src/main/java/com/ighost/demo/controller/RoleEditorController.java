package com.ighost.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.model.RoleDto;
import com.ighost.demo.service.FunctionService;
import com.ighost.demo.service.RoleService;
import com.ighost.demo.service.UserService;

@Controller
public class RoleEditorController {

	@Autowired
	private RoleService roleService;

	@Autowired
	private FunctionService functionService;

	@Autowired
	private UserService userService;

	@GetMapping("/role-edit")
	public String showRoleEditor(@RequestParam(name = "roleId", required = false) String roleId, Model model,
			Principal principal) {

		List<FunctionDto> allFunctions = functionService.findAllFunctions();
		model.addAttribute("allFunctions", allFunctions);

		if (roleId != null && !roleId.isEmpty()) {
			// 處理 Optional<RoleDto>
			roleService.findRoleById(roleId).ifPresentOrElse(role -> model.addAttribute("role", role), // 找到時執行的 Lambda
					() -> { // 找不到時執行的 Lambda
						model.addAttribute("error", "找不到角色代碼：" + roleId);
						model.addAttribute("role", new RoleDto(null, null, null)); // 傳入一個新的空 RoleDto
					});
		} else {
			// 如果沒有 roleId，也傳入一個新的空 RoleDto
			model.addAttribute("role", new RoleDto(null, null, null));
		}

		List<FunctionDto> userFunctions = userService.getFunctionsByUsername(principal.getName());
		List<String> userGroups = userService.getDistinctGroupsByUsername(principal.getName());
		model.addAttribute("functions", userFunctions);

		model.addAttribute("groups", userGroups);

		return "role-edit";
	}

	@PostMapping("/role-edit")
	public String handleSaveRole(@RequestParam("id") String roleId, @RequestParam("name") String roleName,
			@RequestParam(name = "functionIds", required = false) List<Integer> functionIds,
			RedirectAttributes redirectAttributes) {

		try {
			if (roleId == null || roleId.trim().isEmpty()) {
				throw new IllegalArgumentException("角色代碼不可為空！");
			}
			roleService.saveRole(roleId, roleName, functionIds);
			redirectAttributes.addFlashAttribute("successMessage", "角色 [" + roleName + "] 儲存成功！");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "儲存失敗：" + e.getMessage());
		}

		return "redirect:/role-edit?roleId=" + roleId;
	}
}