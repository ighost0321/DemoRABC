package com.ighost.demo.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ighost.demo.entity.ActivityLog;
import com.ighost.demo.repo.ActivityLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {
    
    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 記錄使用者活動
     */
    @Transactional
    public void logActivity(String username, String actionType, String requestUrl, 
                           String requestParameters, String ipAddress, String userAgent) {
        try {
            ActivityLog activityLog = ActivityLog.builder()
                    .username(username)
                    .actionType(actionType)
                    .requestUrl(requestUrl)
                    .requestParameters(requestParameters)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            
            activityLogRepository.save(activityLog);
            log.info("Activity logged: {} - {} - {}", username, actionType, requestUrl);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 從 HttpServletRequest 記錄活動
     */
    @Transactional
    public void logActivity(HttpServletRequest request, String actionType) {
        String username = getCurrentUsername();
        String requestUrl = request.getRequestURI();
        String requestParameters = extractRequestParameters(request);
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        logActivity(username, actionType, requestUrl, requestParameters, ipAddress, userAgent);
    }
    
    /**
     * 記錄登入成功
     */
    @Transactional
    public void logLoginSuccess(String username, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        logActivity(username, ActivityLog.LOGIN_SUCCESS, "/login", null, ipAddress, userAgent);
    }
    
    /**
     * 記錄登入失敗
     */
    @Transactional
    public void logLoginFailure(String username, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        logActivity(username, ActivityLog.LOGIN_FAIL, "/login", null, ipAddress, userAgent);
    }
    
    /**
     * 記錄登出
     */
    @Transactional
    public void logLogout(String username, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        logActivity(username, ActivityLog.LOGOUT, "/logout", null, ipAddress, userAgent);
    }
    
    /**
     * 查詢活動日誌
     */
    public Page<ActivityLog> getActivityLogs(String username, String actionType, 
                                           LocalDateTime startDate, LocalDateTime endDate, 
                                           Pageable pageable) {
        // 處理空字串，轉為 null
        String cleanUsername = (username != null && username.trim().isEmpty()) ? null : username;
        String cleanActionType = (actionType != null && actionType.trim().isEmpty()) ? null : actionType;
        
        return activityLogRepository.findByFilters(cleanUsername, cleanActionType, startDate, endDate, pageable);
    }
    
    /**
     * 檢查登入失敗次數（安全性檢查）
     */
    public boolean isLoginFailureExceeded(String username, int maxAttempts, int minutesBack) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
        Long failureCount = activityLogRepository.countLoginFailuresSince(username, since);
        return failureCount >= maxAttempts;
    }
    
    /**
     * 清理舊記錄
     */
    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        activityLogRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Cleaned up activity logs older than {} days", daysToKeep);
    }
    /**
     * 取得今日活躍使用者數量
     */
    public Long countActiveUsersToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return activityLogRepository.countActiveUsersToday(startOfDay, endOfDay);
    }
    
    // 輔助方法
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
    
    private String extractRequestParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.isEmpty()) {
            return null;
        }
        
        try {
            // 過濾敏感資訊
            Map<String, Object> filteredParams = new HashMap<>();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                if (!isSensitiveParameter(key)) {
                    filteredParams.put(key, entry.getValue().length == 1 ? 
                        entry.getValue()[0] : entry.getValue());
                } else {
                    filteredParams.put(key, "***HIDDEN***");
                }
            }
            return objectMapper.writeValueAsString(filteredParams);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize request parameters: {}", e.getMessage());
            return "Error serializing parameters";
        }
    }
    
    private boolean isSensitiveParameter(String paramName) {
        return paramName.toLowerCase().contains("password") ||
               paramName.toLowerCase().contains("token") ||
               paramName.toLowerCase().contains("secret");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}