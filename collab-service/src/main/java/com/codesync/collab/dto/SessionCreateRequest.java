package com.codesync.collab.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionCreateRequest {

    @NotNull
    private Integer projectId;

    @NotNull
    private Integer fileId;

    private String language;

    private Integer maxParticipants;

    private Boolean isPasswordProtected;

    private String sessionPassword;
}
