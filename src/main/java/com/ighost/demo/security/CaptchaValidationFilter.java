package com.ighost.demo.security;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    private static final String LOGIN_URL = "/login";
    private static final String CAPTCHA_PARAMETER = "captcha";
    private static final int MAX_CAPTCHA_LENGTH = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = TimeUnit.MINUTES.toMillis(5);

    private final AuthenticationFailureHandler failureHandler;
    
    // Simple in-memory rate limiting (consider Redis for production/distributed systems)
    private final ConcurrentHashMap<String, AttemptRecord> attemptTracker = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (isLoginRequest(request)) {
            String clientId = getClientIdentifier(request);
            
            // Check if client is locked out due to too many failed attempts
            if (isLockedOut(clientId)) {
                failureHandler.onAuthenticationFailure(request, response,
                        new CaptchaValidationException("Too many failed attempts. Please try again later."));
                return;
            }

            if (!isCaptchaValid(request)) {
                recordFailedAttempt(clientId);
                failureHandler.onAuthenticationFailure(request, response,
                        new CaptchaValidationException("Captcha validation failed"));
                return;
            }
            
            // Reset attempts on successful captcha validation
            resetAttempts(clientId);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return LOGIN_URL.equals(request.getServletPath())
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private boolean isCaptchaValid(HttpServletRequest request) {
        String userInput = request.getParameter(CAPTCHA_PARAMETER);
        
        // Validate input length to prevent abuse
        if (!StringUtils.hasText(userInput) || userInput.length() > MAX_CAPTCHA_LENGTH) {
            return false;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object stored = session.getAttribute(CaptchaController.CAPTCHA_SESSION_KEY);
        
        // Remove captcha from session immediately to prevent replay attacks
        // User must generate new captcha on failure
        session.removeAttribute(CaptchaController.CAPTCHA_SESSION_KEY);

        if (!(stored instanceof String expectedCaptcha) || !StringUtils.hasText(expectedCaptcha)) {
            return false;
        }

        // Case-sensitive comparison with normalized whitespace for both sides
        return expectedCaptcha.trim().equals(userInput.trim());
    }

    /**
     * Gets a unique identifier for rate limiting
     * Uses IP address - consider combining with session ID for better accuracy
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        } else {
            // X-Forwarded-For can contain multiple IPs, get the first one
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    private boolean isLockedOut(String clientId) {
        AttemptRecord user = attemptTracker.get(clientId);
        if (user == null) {
            return false;
        }

        // Clean up expired lockouts
        if (System.currentTimeMillis() - user.firstAttemptTime > LOCKOUT_DURATION_MS) {
            attemptTracker.remove(clientId);
            return false;
        }

        return user.attempts >= MAX_ATTEMPTS;
    }

    private void recordFailedAttempt(String clientId) {
        attemptTracker.compute(clientId, (key, client) -> {
            if (client == null) {
                return new AttemptRecord(1, System.currentTimeMillis());
            }
            
            // Reset if lockout period has passed
            if (System.currentTimeMillis() - client.firstAttemptTime > LOCKOUT_DURATION_MS) {
                return new AttemptRecord(1, System.currentTimeMillis());
            }
            
            return new AttemptRecord(client.attempts + 1, client.firstAttemptTime);
        });
    }

    private void resetAttempts(String clientId) {
        attemptTracker.remove(clientId);
    }

    /**
     * Record for tracking failed captcha attempts
     */
    private record AttemptRecord(int attempts, long firstAttemptTime) {}
}