package com.ighost.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public String welcome(HttpSession session, Principal principal) {
		List<FunctionDto> functions = userService.getFunctionsByUsername(principal.getName());
		List<String> groups = userService.getDistinctGroupsByUsername(principal.getName());
		session.setAttribute("groups", groups);
        session.setAttribute("functions", functions);
		return "welcome";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}
}
