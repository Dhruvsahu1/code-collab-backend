package com.codesync.collab.dto;
 
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
 
@Getter
@Setter
@NoArgsConstructor
public class CollabSessionRequest {
  
    private Long projectId;
    private Long fileId;
    private Long ownerId;
    private String language;
    private String code;
    private Integer maxParticipants;
    private Boolean isPasswordProtected;
    private String sessionPassword;
    private String projectName;
    private String fileName;
}
 