package com.ighost.demo.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.ighost.demo.service.ActivityLogService;

/**
 * 登出成功處理器
 */
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final String LOGOUT_REDIRECT_URL = "/login?logout=true";

    private final ActivityLogService activityLogService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (authentication != null) {
            String username = authentication.getName();
            activityLogService.logLogout(username, request);
        }

        response.sendRedirect(LOGOUT_REDIRECT_URL);
    }
}
