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
    public String queryRoles(@RequestParam(required = false) String keyword, Model model) {
    	List<RoleDto> roles = roleService.findByRoleOrFunction(keyword);
        model.addAttribute("keyword", keyword);
        model.addAttribute("roles", roles);
        return "role-query";
    }
}

