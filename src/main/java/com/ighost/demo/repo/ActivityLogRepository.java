package com.ighost.demo.repo;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ighost.demo.entity.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    // 根據使用者名稱查詢
    Page<ActivityLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    
    // 根據操作類型查詢
    Page<ActivityLog> findByActionTypeOrderByCreatedAtDesc(String actionType, Pageable pageable);
    
    // 根據時間範圍查詢
    @Query("SELECT a FROM ActivityLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate, 
                                     Pageable pageable);
    
    // 綜合查詢 - 使用原生 SQL 避免 PostgreSQL 型別推斷問題
    @Query(value = "SELECT * FROM activity_logs a WHERE " +
           "(:username IS NULL OR :username = '' OR a.username = :username) AND " +
           "(:actionType IS NULL OR :actionType = '' OR a.action_type = :actionType) AND " +
           // 修復點：為 IS NULL 條件添加類型轉換 ::timestamp
           "((CAST(:startDate AS timestamp)) IS NULL OR a.created_at >= :startDate) AND " +
           "((CAST(:endDate AS timestamp)) IS NULL OR a.created_at <= :endDate) " +
           "ORDER BY a.created_at DESC",
           nativeQuery = true)
    Page<ActivityLog> findByFilters(@Param("username") String username,
                                   @Param("actionType") String actionType,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);
    
    // 統計查詢 - 登入失敗次數
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.username = :username AND a.actionType = 'LOGIN_FAIL' AND a.createdAt >= :since")
    Long countLoginFailuresSince(@Param("username") String username, @Param("since") LocalDateTime since);
    
    // 統計查詢 - 每日活躍使用者
    @Query("SELECT COUNT(DISTINCT a.username) FROM ActivityLog a WHERE a.createdAt >= :startOfDay AND a.createdAt < :endOfDay")
    Long countActiveUsersToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // 刪除舊記錄（清理資料用）
    void deleteByCreatedAtBefore(LocalDateTime date);
}