package com.ighost.demo.security;

import java.io.IOException;

import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ighost.demo.controller.CaptchaController;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CaptchaValidationFilter extends OncePerRequestFilter {

    private static final String LOGIN_PROCESSING_URL = "/login";
    private static final String CAPTCHA_PARAMETER = "captcha";

    private final AuthenticationFailureHandler failureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (isLoginRequest(request)) {
            if (!isCaptchaValid(request)) {
                failureHandler.onAuthenticationFailure(request, response,
                        new CaptchaValidationException("Captcha validation failed"));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return LOGIN_PROCESSING_URL.equals(request.getServletPath())
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private boolean isCaptchaValid(HttpServletRequest request) {
        String userInput = request.getParameter(CAPTCHA_PARAMETER);
        HttpSession session = request.getSession(false);
        String expectedCaptcha = session != null
                ? (String) session.getAttribute(CaptchaController.CAPTCHA_SESSION_KEY)
                : null;

        if (session != null) {
            session.removeAttribute(CaptchaController.CAPTCHA_SESSION_KEY);
        }

        return StringUtils.hasText(userInput)
                && StringUtils.hasText(expectedCaptcha)
                && expectedCaptcha.equalsIgnoreCase(userInput.trim());
    }
}
