package com.ighost.demo.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.ighost.demo.service.ActivityLogService;
import com.ighost.demo.util.RequestUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final ActivityLogService activityLogService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        String ipAddress = RequestUtils.getCurrentRequestIpAddress();
        activityLogService.logActivity(username, "LOGIN", "User logged in successfully", ipAddress);
    }
}
