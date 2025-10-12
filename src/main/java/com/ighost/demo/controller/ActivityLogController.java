package com.ighost.demo.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.ighost.demo.entity.ActivityLog;
import com.ighost.demo.service.ActivityLogService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/activity-log-query")
@RequiredArgsConstructor
public class ActivityLogController {

    private static final String VIEW_ACTIVITY_LOGS = "activity-logs";
    private static final String VIEW_STATS = "log-stats";
    private static final String ACTION_DATA_MODIFICATION = "DATA_MODIFICATION";

    private final ActivityLogService activityLogService;

    /**
     * 顯示活動日誌查詢頁面
     */
    @GetMapping
    public String showActivityLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> logs = activityLogService.getActivityLogs(username, actionType, startDate, endDate, pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("username", username);
        model.addAttribute("actionType", actionType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        int totalPages = logs.getTotalPages();
        int currentPageDisplay = logs.getNumber() + 1;
        int[] pageWindow = calculatePageWindow(currentPageDisplay, totalPages);
        model.addAttribute("currentPageDisplay", currentPageDisplay);
        model.addAttribute("pageStart", pageWindow[0]);
        model.addAttribute("pageEnd", pageWindow[1]);

        model.addAttribute("actionTypes", List.of(
                ActivityLog.LOGIN_SUCCESS,
                ActivityLog.LOGIN_FAIL,
                ActivityLog.FUNCTION_ACCESS,
                ActivityLog.LOGOUT,
                ACTION_DATA_MODIFICATION));

        model.addAttribute("groups", Collections.emptyList());
        model.addAttribute("functions", Collections.emptyList());

        return VIEW_ACTIVITY_LOGS;
    }

    private int[] calculatePageWindow(int currentPage, int totalPages) {
        if (totalPages <= 0) {
            return new int[] { 0, 0 };
        }

        final int windowSize = 10;
        int start = Math.max(1, currentPage - windowSize / 2);
        int end = Math.min(totalPages, start + windowSize - 1);

        if (end - start + 1 < windowSize) {
            start = Math.max(1, end - windowSize + 1);
        }

        return new int[] { start, end };
    }
    
    /**
     * 導出活動日誌 (CSV 格式)
     */
    @GetMapping("/export")
    public String exportLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/activity-log-query");
        if (username != null && !username.isBlank()) {
            builder.queryParam("username", username);
        }
        if (actionType != null && !actionType.isBlank()) {
            builder.queryParam("actionType", actionType);
        }
        if (startDate != null) {
            builder.queryParam("startDate", startDate);
        }
        if (endDate != null) {
            builder.queryParam("endDate", endDate);
        }

        return "redirect:" + builder.toUriString();
    }
    
    /**
     * 系統統計頁面
     */
    @GetMapping("/stats")
    public String showStats() {
        return VIEW_STATS;
    }
}
