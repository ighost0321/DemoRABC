package com.ighost.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ighost.demo.entity.ActivityLog;
import com.ighost.demo.repo.ActivityLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogCommandService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void saveLog(ActivityLog activityLog) {
        activityLogRepository.save(activityLog);
        log.info("Activity logged: {} - {} - {}", activityLog.getUsername(), activityLog.getActionType(), activityLog.getRequestUrl());
    }

    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        activityLogRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Cleaned up activity logs older than {} days", daysToKeep);
    }
}
