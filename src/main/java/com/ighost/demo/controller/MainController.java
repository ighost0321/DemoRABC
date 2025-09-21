package com.ighost.demo.controller;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	@Autowired
	private UserService userService;

	@GetMapping("/")
	public String welcome(HttpSession session, Principal principal) {
		String username = principal.getName();
		logger.info("--- Sidebar Data Debug ---");
		logger.info("Principal username: {}", username);

		List<FunctionDto> functions = userService.getFunctionsByUsername(username);
		logger.info("Functions found: {}", functions.size());

		List<String> groups = functions.stream()
                .map(FunctionDto::groupName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
		logger.info("Groups found: {}", groups.size());
		logger.info("Group names: {}", groups);
		logger.info("--- End Debug ---");

		session.setAttribute("groups", groups);
        session.setAttribute("functions", functions);
		return "welcome";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}
}
