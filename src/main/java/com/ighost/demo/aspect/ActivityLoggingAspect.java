package com.ighost.demo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ighost.demo.entity.ActivityLog;
import com.ighost.demo.service.ActivityLogService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLoggingAspect {

    private static final String ACTION_DATA_MODIFICATION = "DATA_MODIFICATION";

    private final ActivityLogService activityLogService;

    // 攔截所有 Controller 的方法（除了登入登出相關）
    @Pointcut("execution(* com.ighost.demo.controller.*.*(..)) && "
            + "!execution(* com.ighost.demo.controller.*.*login*(..)) && "
            + "!execution(* com.ighost.demo.controller.*.*logout*(..)) && "
            + "!execution(* com.ighost.demo.controller.ActivityLogController.*(..))")
    public void controllerMethods() {
    }
    
    // 攔截 GET 請求（頁面存取）
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void getMappings() {
    }
    
    // 攔截 POST/PUT/DELETE 請求（資料操作）
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void dataModificationMappings() {
    }
    
    /**
     * 記錄功能存取（GET 請求）
     */
    @AfterReturning("controllerMethods() && getMappings()")
    public void logFunctionAccess() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null && !isStaticResource(request) && !isAjaxRequest(request)) {
                activityLogService.logActivity(request, ActivityLog.FUNCTION_ACCESS);
            }
        } catch (Exception e) {
            log.error("Error logging function access", e);
        }
    }
    
    /**
     * 記錄資料操作
     */
    @AfterReturning("controllerMethods() && dataModificationMappings()")
    public void logDataModification() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                activityLogService.logActivity(request, ACTION_DATA_MODIFICATION);
            }
        } catch (Exception e) {
            log.error("Error logging data modification", e);
        }
    }
    
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private boolean isStaticResource(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri.contains("/css/")
                || requestUri.contains("/js/")
                || requestUri.contains("/images/")
                || requestUri.contains("/favicon.ico");
    }
    
    private boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(xRequestedWith);
    }
}
