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
        
        // 修正：使用正確的 6 個參數調用 logActivity
        activityLogService.logActivity(
            username,               // username
            "LOGIN_SUCCESS",        // actionType 
            "/login",              // requestUrl
            null,                  // requestParameters
            ipAddress,             // ipAddress
            null                   // userAgent (從 ApplicationEvent 無法取得)
        );
    }
}