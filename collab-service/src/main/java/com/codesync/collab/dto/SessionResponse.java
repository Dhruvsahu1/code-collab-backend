package com.codesync.collab.dto;

import com.codesync.collab.entity.CollabSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String sessionId;
    private Long projectId;
    private Long fileId;
    private Long ownerId;
    private String status;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private int maxParticipants;
    private boolean isPasswordProtected;

    public static SessionResponse fromEntity(CollabSession session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .projectId(session.getProjectId())
                .fileId(session.getFileId())
                .ownerId(session.getOwnerId())
                .status(session.getStatus().name())
                .language(session.getLanguage())
                .createdAt(session.getCreatedAt())
                .endedAt(session.getEndedAt())
                .maxParticipants(session.getMaxParticipants())
                .isPasswordProtected(session.isPasswordProtected())
                .build();
    }
}