package com.ighost.demo.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ighost.demo.entity.ActivityLog;
import com.ighost.demo.service.ActivityLogService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/activity-log-query")
@RequiredArgsConstructor
public class ActivityLogController {
    
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
        
        // 提供操作類型選項
        model.addAttribute("actionTypes", new String[]{
            ActivityLog.LOGIN_SUCCESS, 
            ActivityLog.LOGIN_FAIL, 
            ActivityLog.FUNCTION_ACCESS, 
            ActivityLog.LOGOUT,
            "DATA_MODIFICATION"
        });
        
        // 為 sidebar 提供必要的參數
        model.addAttribute("groups", new java.util.ArrayList<>());
        model.addAttribute("functions", new java.util.ArrayList<>());
        
        return "activity-logs";
    }
    
    /**
     * 導出活動日誌 (CSV 格式)
     */
    @GetMapping("/export")
    public String exportLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Model model) {
        
        // 這裡可以實作 CSV 導出邏輯
        // 為了簡化，先重導向到查詢頁面
        return "redirect:/activity-log-query?username=" + (username != null ? username : "") +
               "&actionType=" + (actionType != null ? actionType : "");
    }
    
    /**
     * 系統統計頁面
     */
    @GetMapping("/stats")
    public String showStats(Model model) {
        // 這裡可以加入各種統計資訊
        // 例如：每日登入數、功能使用統計等
        return "log-stats";
    }
}