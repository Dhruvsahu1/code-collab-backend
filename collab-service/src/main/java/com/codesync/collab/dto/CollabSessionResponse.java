package com.codesync.collab.dto;
 
import com.codesync.collab.model.CollabSession.SessionStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Getter
@Setter
@NoArgsConstructor
public class CollabSessionResponse {
 
    private Long id;
    private String sessionId;
    private Long projectId;
    private Long fileId;
    private Long ownerId;
    private String code;
    private SessionStatus status;
    private String language;
    private Integer maxParticipants;
    private Boolean isPasswordProtected;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endedAt;
    private Long participantCount;
    private String projectName;
    private String fileName;
}