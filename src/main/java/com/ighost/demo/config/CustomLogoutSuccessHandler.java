package com.ighost.demo.config;

import com.ighost.demo.service.ActivityLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 登出成功處理器
 */
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    
    private final ActivityLogService activityLogService;
    
    @Override
    public void onLogoutSuccess(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Authentication authentication) throws IOException, ServletException {
        
        // 記錄登出
        if (authentication != null) {
            String username = authentication.getName();
            activityLogService.logLogout(username, request);
        }
        
        // 導向登入頁面
        response.sendRedirect("/login?logout=true");
    }
}