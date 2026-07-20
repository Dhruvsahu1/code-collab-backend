package com.codesync.collab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.codesync.collab.model.Participant.ParticipantRole;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {

    private Long participantId;
    private String sessionId;
    private Long userId;
    private ParticipantRole role;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Boolean isActive;
    private Integer cursorLine;
    private Integer cursorCol;
    private String color;
}
