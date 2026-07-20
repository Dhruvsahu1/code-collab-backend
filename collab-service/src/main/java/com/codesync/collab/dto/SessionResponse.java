package com.codesync.collab.dto;

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
    private Integer projectId;
    private Integer fileId;
    private Integer ownerId;
    private String status;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private Integer maxParticipants;
    private Boolean isPasswordProtected;
    private Long currentVersion;
}
