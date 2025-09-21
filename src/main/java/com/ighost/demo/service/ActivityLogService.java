package com.ighost.demo.service;

import org.springframework.stereotype.Service;
import com.ighost.demo.entity.ActivityLog;
import com.ighost.demo.repo.ActivityLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void logActivity(String username, String actionType, String actionDetails, String ipAddress) {
        ActivityLog log = new ActivityLog(username, actionType, actionDetails, ipAddress);
        activityLogRepository.save(log);
    }
}
