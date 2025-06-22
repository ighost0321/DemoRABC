package com.ighost.demo.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

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

	// *** 關鍵點 1：注入 UserService 以取得使用者權限 ***
	@Autowired
	private UserService userService;

	@GetMapping("/role-edit")
	public String showRoleEditor(@RequestParam(name = "roleId", required = false) String roleId, Model model,
			Principal principal) {

		// 查詢所有可用的功能，以供權限表格使用
		List<FunctionDto> allFunctions = functionService.findAllFunctions();
		model.addAttribute("allFunctions", allFunctions);

		// 根據傳入的 roleId 查詢角色資料
		if (roleId != null && !roleId.isEmpty()) {
			Optional<RoleDto> roleOpt = roleService.findRoleById(roleId);
			if (roleOpt.isPresent()) {
				model.addAttribute("role", roleOpt.get());
			} else {
				model.addAttribute("error", "找不到角色代碼：" + roleId);
				model.addAttribute("role", new RoleDto());
			}
		} else {
			model.addAttribute("role", new RoleDto());
		}

		// *** 關鍵點 2：取得並傳遞側邊欄所需的資料 ***
		// 為了讓 sidebar.html 能正確渲染，必須提供 userFunctions 和 userGroups
		List<FunctionDto> userFunctions = userService.getFunctionsByUsername(principal.getName());
		List<String> userGroups = userService.getDistinctGroupsByUsername(principal.getName());
		model.addAttribute("functions", userFunctions);
		model.addAttribute("groups", userGroups);

		return "role-edit";
	}
	
	// *** 3. 新增 POST 方法來處理表單提交 ***
    @PostMapping("/role-edit")
    public String handleSaveRole(
            @RequestParam("id") String roleId,
            @RequestParam("name") String roleName,
            @RequestParam(name = "functionIds", required = false) List<Integer> functionIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            roleService.saveRole(roleId, roleName, functionIds);
            // 使用 RedirectAttributes 來傳遞成功訊息
            redirectAttributes.addFlashAttribute("successMessage", "角色 [" + roleName + "] 儲存成功！");
        } catch (Exception e) {
            // 如果發生錯誤，傳遞錯誤訊息
            redirectAttributes.addFlashAttribute("errorMessage", "儲存失敗：" + e.getMessage());
        }

        // 儲存後，重新導向到編輯頁面，並帶上剛才儲存的 roleId，以便立即看到結果
        return "redirect:/role-edit?roleId=" + roleId;
    }
}