package com.ighost.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.model.RoleDto;
import com.ighost.demo.service.FunctionService;
import com.ighost.demo.service.RoleService;
import com.ighost.demo.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoleEditorController {

    private static final String ROLE_EDIT_VIEW = "role-edit";
    private static final String SUCCESS_MESSAGE_KEY = "successMessage";
    private static final String ERROR_MESSAGE_KEY = "errorMessage";

    private final RoleService roleService;
    private final FunctionService functionService;
    private final UserService userService;

    @GetMapping("/role-edit")
    public String showRoleEditor(@RequestParam(name = "roleId", required = false) String roleId,
            Model model,
            Principal principal) {

        model.addAttribute("allFunctions", functionService.findAllFunctions());

        if (StringUtils.hasText(roleId)) {
            roleService.findRoleById(roleId)
                    .ifPresentOrElse(role -> model.addAttribute("role", role), () -> {
                        model.addAttribute("error", "找不到角色代碼：" + roleId);
                        model.addAttribute("role", createEmptyRoleDto());
                    });
        } else {
            model.addAttribute("role", createEmptyRoleDto());
        }

        List<FunctionDto> userFunctions = userService.getFunctionsByUsername(principal.getName());
        model.addAttribute("functions", userFunctions);
        model.addAttribute("groups", extractGroups(userFunctions));

        return ROLE_EDIT_VIEW;
    }

    @PostMapping("/role-edit")
    public String handleSaveRole(@RequestParam("id") String roleId,
            @RequestParam("name") String roleName,
            @RequestParam(name = "functionIds", required = false) List<Integer> functionIds,
            RedirectAttributes redirectAttributes) {

        try {
            roleService.saveRole(roleId, roleName, functionIds);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_KEY, "角色 [" + roleName + "] 儲存成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_KEY, "儲存失敗：" + e.getMessage());
        }

        String redirectUrl = UriComponentsBuilder.fromPath("/role-edit")
                .queryParam("roleId", roleId)
                .toUriString();
        return "redirect:" + redirectUrl;
    }

    private List<String> extractGroups(List<FunctionDto> functions) {
        return functions.stream()
                .map(FunctionDto::groupName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private RoleDto createEmptyRoleDto() {
        return new RoleDto(null, null, null);
    }
}
