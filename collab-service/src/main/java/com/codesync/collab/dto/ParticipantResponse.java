package com.codesync.collab.dto;

import com.codesync.collab.entity.Participant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private Long participantId;
    private String sessionId;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private int cursorLine;
    private int cursorCol;
    private String color;

    public static ParticipantResponse fromEntity(Participant participant) {
        return ParticipantResponse.builder()
                .participantId(participant.getParticipantId())
                .sessionId(participant.getSessionId())
                .userId(participant.getUserId())
                .role(participant.getRole().name())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .cursorLine(participant.getCursorLine())
                .cursorCol(participant.getCursorCol())
                .color(participant.getColor())
                .build();
    }
}