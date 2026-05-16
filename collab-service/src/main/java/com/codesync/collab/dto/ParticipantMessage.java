package com.codesync.collab.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantMessage {
    private String sessionId;
    private Long userId;
    private String username;
    private String action;
    private String color;
    private String role;
}