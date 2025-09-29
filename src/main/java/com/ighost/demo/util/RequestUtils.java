package com.ighost.demo.util;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtils {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    private static final String UNKNOWN = "unknown";

    private RequestUtils() {
    }

    public static String getCurrentRequestIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return resolveClientIp(attributes.getRequest());
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (StringUtils.hasText(forwarded) && !UNKNOWN.equalsIgnoreCase(forwarded)) {
            return forwarded.split(",")[0].trim();
        }

        String realIp = request.getHeader(HEADER_X_REAL_IP);
        if (StringUtils.hasText(realIp) && !UNKNOWN.equalsIgnoreCase(realIp)) {
            return realIp;
        }

        return request.getRemoteAddr();
    }
}
