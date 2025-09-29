package com.ighost.demo.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ighost.demo.entity.ActivityLog;
import com.ighost.demo.service.ActivityLogService;
import com.ighost.demo.util.RequestUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final String LOGIN_URL = "/login";

    private final ActivityLogService activityLogService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            log.warn("Unsupported principal type: {}", principal.getClass());
            return;
        }

        String username = userDetails.getUsername();
        String ipAddress = RequestUtils.getCurrentRequestIpAddress();

        activityLogService.logActivity(
                username,
                ActivityLog.LOGIN_SUCCESS,
                LOGIN_URL,
                null,
                ipAddress,
                null);
    }
}
