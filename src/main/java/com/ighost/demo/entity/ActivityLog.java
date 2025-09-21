package com.ighost.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activity_log")
@Data
@NoArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "action_details", length = 255)
    private String actionDetails;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    public ActivityLog(String username, String actionType, String actionDetails, String ipAddress) {
        this.username = username;
        this.actionType = actionType;
        this.actionDetails = actionDetails;
        this.ipAddress = ipAddress;
        this.actionTime = LocalDateTime.now();
    }
}
