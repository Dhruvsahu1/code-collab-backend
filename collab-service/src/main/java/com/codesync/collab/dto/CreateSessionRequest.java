package com.codesync.collab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    private Long projectId;
    private Long fileId;
    private String language;
    private String password;
    private int maxParticipants;
}