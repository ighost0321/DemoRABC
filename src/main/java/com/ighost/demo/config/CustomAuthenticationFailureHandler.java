package com.ighost.demo.config;

import com.ighost.demo.service.ActivityLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 登入失敗處理器
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    private final ActivityLogService activityLogService;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       AuthenticationException exception) throws IOException, ServletException {
        
        // 記錄登入失敗
        String username = request.getParameter("username");
        if (username != null) {
            activityLogService.logLoginFailure(username, request);
            
            // 檢查是否超過失敗次數限制
            if (activityLogService.isLoginFailureExceeded(username, 5, 15)) {
                // 可以在這裡實作帳號鎖定邏輯
                response.sendRedirect("/login?error=account_locked");
                return;
            }
        }
        
        // 導向登入頁面並顯示錯誤訊息
        response.sendRedirect("/login?error=true");
    }
}