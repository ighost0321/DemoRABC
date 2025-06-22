package com.ighost.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

	@Autowired
	private RoleService roleService;

    @Autowired
    private UserService userService;

    // 從 application.yaml 讀取每頁顯示的筆數
    @Value("${custom.pagination.page-size}")
    private int pageSize;

	@GetMapping("/role-query")
	public String queryRoles(@RequestParam(value = "keyword", required = false) String keyword,
			                 @RequestParam(value = "page", defaultValue = "1") int page,
			                 Model model,
                             Principal principal) {

		// 1. 取得符合條件的角色總數 (回傳型別從 int 變為 long)
		long totalRoles = roleService.countByKeyword(keyword);
		
        // 2. 使用 long 型別的 totalRoles 來計算總頁數，避免型別轉換問題
		int totalPages = (int) Math.ceil((double) totalRoles / pageSize);

		// 3. 確保頁碼在有效範圍內
		if (page < 1) {
			page = 1;
        }
		if (page > totalPages && totalPages > 0) {
			page = totalPages;
        }

		// 4. 呼叫 Service 層取得當前頁面的角色資料
		List<RoleDto> roles = roleService.findByKeyword(keyword, page, pageSize);

		// 5. 將所有需要的資料加入到 Model 中，供前端樣板使用
		model.addAttribute("roles", roles);
		model.addAttribute("keyword", keyword);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);

        // 6. 為了讓側邊欄能正確顯示，傳遞使用者權限資料
        List<FunctionDto> userFunctions = userService.getFunctionsByUsername(principal.getName());
        List<String> userGroups = userService.getDistinctGroupsByUsername(principal.getName());
        model.addAttribute("functions", userFunctions);
        model.addAttribute("groups", userGroups);

		return "role-query";
	}
}