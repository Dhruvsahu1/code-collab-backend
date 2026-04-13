package com.codesync.collab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeChangeRequest {
    private Long userId;
    private String changeType;
    private String content;
    private int startLine;
    private int endLine;
    private int startCol;
    private int endCol;
}