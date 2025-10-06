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

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.service.FunctionGroupService;
import com.ighost.demo.service.FunctionService;
import com.ighost.demo.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FunctionEditorController {

    private static final String VIEW_NAME = "function-edtior";
    private static final String CREATE_VIEW_NAME = "function-create";
    private static final String FORM_DATA_ATTR = "formData";

    private final FunctionService functionService;
    private final FunctionGroupService functionGroupService;
    private final UserService userService;

    @GetMapping("/function-edtior")
    public String showFunctionEditor(@RequestParam(value = "functionId", required = false) Integer functionId,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model,
            Principal principal) {

        List<FunctionDto> searchResults = functionService.searchFunctions(functionId, keyword);

        model.addAttribute("selectedFunctionId", functionId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("groupOptions", functionGroupService.getAllGroups());

        populateSidebar(model, principal);

        return VIEW_NAME;
    }

    @PostMapping("/function-edtior")
    public String updateFunction(@RequestParam("id") Integer id,
            @RequestParam("groupId") Integer groupId,
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("url") String url,
            @RequestParam(value = "keyword", required = false) String keyword,
            RedirectAttributes redirectAttributes) {

        try {
            functionService.updateFunction(id, groupId, code, name, url);
            redirectAttributes.addFlashAttribute("successMessage", "功能更新成功。");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addAttribute("functionId", id);
        }

        if (StringUtils.hasText(keyword)) {
            redirectAttributes.addAttribute("keyword", keyword.trim());
        }

        return "redirect:/function-edtior";
    }

    @GetMapping("/function-edtior/new")
    public String showCreateForm(Model model, Principal principal) {
        model.addAttribute("groupOptions", functionGroupService.getAllGroups());
        populateSidebar(model, principal);

        if (!model.containsAttribute(FORM_DATA_ATTR)) {
            model.addAttribute(FORM_DATA_ATTR, new FunctionDto(null, null, null, null, null, null));
        }

        return CREATE_VIEW_NAME;
    }

    @PostMapping("/function-edtior/new")
    public String createFunction(@RequestParam("groupId") Integer groupId,
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("url") String url,
            RedirectAttributes redirectAttributes) {

        String sanitizedCode = code != null ? code.trim() : null;
        String sanitizedName = name != null ? name.trim() : null;
        String sanitizedUrl = url != null ? url.trim() : null;

        try {
            FunctionDto createdFunction = functionService.createFunction(groupId, sanitizedCode, sanitizedName, sanitizedUrl);
            redirectAttributes.addFlashAttribute("successMessage", "功能新增成功。");
            redirectAttributes.addAttribute("functionId", createdFunction.id());
            return "redirect:/function-edtior";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute(FORM_DATA_ATTR, new FunctionDto(null, sanitizedCode, sanitizedName, sanitizedUrl, groupId, null));
            return "redirect:/function-edtior/new";
        }
    }

    private List<String> extractGroups(List<FunctionDto> functions) {
        return functions.stream()
                .map(FunctionDto::groupName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private void populateSidebar(Model model, Principal principal) {
        List<FunctionDto> userFunctions = userService.getFunctionsByUsername(principal.getName());
        model.addAttribute("functions", userFunctions);
        model.addAttribute("groups", extractGroups(userFunctions));
    }
}
