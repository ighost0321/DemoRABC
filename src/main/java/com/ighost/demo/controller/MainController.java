package com.ighost.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final UserService userService;

    @GetMapping("/")
    public String welcome(HttpSession session, Principal principal) {
        String username = principal.getName();
        log.debug("Preparing sidebar data for user {}", username);

        List<FunctionDto> functions = userService.getFunctionsByUsername(username);
        List<String> groups = extractGroups(functions);

        session.setAttribute("functions", List.copyOf(functions));
        session.setAttribute("groups", groups);
        return "welcome";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private List<String> extractGroups(List<FunctionDto> functions) {
        return functions.stream()
                .map(FunctionDto::groupName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }
}
