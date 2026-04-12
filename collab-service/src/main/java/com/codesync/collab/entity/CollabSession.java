package com.codesync.collab.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "collab_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollabSession {

    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "max_participants")
    private int maxParticipants;

    @Column(name = "is_password_protected")
    private boolean isPasswordProtected;

    @Column(name = "session_password")
    private String sessionPassword;

    public enum SessionStatus {
        ACTIVE,
        ENDED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = SessionStatus.ACTIVE;
        }
    }
}