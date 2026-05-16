package com.codesync.collab.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Full session state sent to newly joining users to sync their editor.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollabSessionState {
    private String sessionId;
    private String code;
    private Long version;
    private String language;
    private Long projectId;
    private Long fileId;
    private String projectName;
    private String fileName;
    private List<Map<String, Object>> participants;
    private Map<String, Map<String, Object>> cursors;
}
