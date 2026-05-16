package com.codesync.collab.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CursorMessage {
    private String sessionId;
    private Long userId;
    private Integer line;
    private Integer column;
    private String color;
    private Long timestamp;
}