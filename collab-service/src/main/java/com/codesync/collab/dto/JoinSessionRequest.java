package com.codesync.collab.dto;
 
import com.codesync.collab.model.Participant.ParticipantRole;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
 
@Getter
@Setter
@NoArgsConstructor
public class JoinSessionRequest {
 
    private Long userId;
    private ParticipantRole role;       // EDITOR or VIEWER (HOST is set automatically for owner)
    private String sessionPassword;     // required only if session is password-protected
}
 