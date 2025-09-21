package com.ighost.demo.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import com.ighost.demo.service.ActivityLogService;
import com.ighost.demo.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;

    @AfterReturning(pointcut = "execution(* com.ighost.demo.service.RoleService.saveRole(..)) && args(roleId, roleName, ..)", returning = "result")
    public void logSaveRole(Object result, String roleId, String roleName) {
        String username = "SYSTEM"; // Default username
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        }

        String ipAddress = RequestUtils.getCurrentRequestIpAddress();
        String details = String.format("Saved role [ID: %s, Name: %s]", roleId, roleName);
        activityLogService.logActivity(username, "SAVE_ROLE", details, ipAddress);
    }
}
