package com.codesync.collab.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeChangeMessage {
    private String sessionId;
    private Long userId;
    private Long fileId;
    private String content;
    private Long timestamp;
    private Long version;
    private String changeType; // FULL_SYNC, DELTA
}