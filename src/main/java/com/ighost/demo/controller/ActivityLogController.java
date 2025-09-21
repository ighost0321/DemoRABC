package com.ighost.demo.controller;

import com.ighost.demo.entity.ActivityLog;
import com.ighost.demo.model.FunctionDto;
import com.ighost.demo.repo.ActivityLogRepository;
import com.ighost.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;
    private final UserService userService;

    @GetMapping("/activity-log-query")
    public String queryLogs(@RequestParam(defaultValue = "1") int page, Model model, Principal principal) {
        int pageSize = 15; // or get from properties
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("actionTime").descending());
        Page<ActivityLog> logPage = activityLogRepository.findAll(pageable);

        model.addAttribute("logPage", logPage);

        // For sidebar
        List<FunctionDto> functions = userService.getFunctionsByUsername(principal.getName());
        List<String> groups = functions.stream()
                .map(FunctionDto::groupName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute("functions", functions);
        model.addAttribute("groups", groups);

        return "activity-log-query";
    }
}
