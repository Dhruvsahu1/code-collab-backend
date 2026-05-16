package com.codesync.collab.dto;
 
import com.codesync.collab.model.Participant.ParticipantRole;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Getter
@Setter
@NoArgsConstructor
public class ParticipantResponse {
 
    private Long participantId;
    private Long userId;
    private ParticipantRole role;
    private String color;
    private Integer cursorLine;
    private Integer cursorCol;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Boolean isActive;
}