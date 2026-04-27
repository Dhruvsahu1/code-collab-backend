package com.codesync.collab.dto;
 
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
 
@Getter
@Setter
@NoArgsConstructor
public class UpdateCursorRequest {
 
    private Long userId;
    private Integer cursorLine;
    private Integer cursorCol;
}