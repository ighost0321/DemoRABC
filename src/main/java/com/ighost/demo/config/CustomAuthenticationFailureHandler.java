package com.ighost.demo.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ighost.demo.security.CaptchaValidationException;
import com.ighost.demo.service.ActivityLogService;

/**
 * 登入失敗處理器
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final int MAX_FAILURE_ATTEMPTS = 5;
    private static final int FAILURE_WINDOW_MINUTES = 15;
    private static final String ACCOUNT_LOCKED_URL = "/login?error=account_locked";
    private static final String GENERIC_LOGIN_ERROR_URL = "/login?error=true";
    private static final String CAPTCHA_ERROR_URL = "/login?error=captcha";

    private final ActivityLogService activityLogService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username");
        boolean isCaptchaError = exception instanceof CaptchaValidationException;

        if (username != null) {
            activityLogService.logLoginFailure(username, request);

            if (!isCaptchaError && activityLogService.isLoginFailureExceeded(
                    username, MAX_FAILURE_ATTEMPTS, FAILURE_WINDOW_MINUTES)) {
                response.sendRedirect(ACCOUNT_LOCKED_URL);
                return;
            }
        }

        if (isCaptchaError) {
            response.sendRedirect(CAPTCHA_ERROR_URL);
            return;
        }

        response.sendRedirect(GENERIC_LOGIN_ERROR_URL);
    }
}
