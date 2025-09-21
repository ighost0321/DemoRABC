package com.ighost.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ighost.demo.entity.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
}
