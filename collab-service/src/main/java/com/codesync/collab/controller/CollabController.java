package com.codesync.collab.controller;

import com.codesync.collab.dto.CollabSessionRequest;
import com.codesync.collab.dto.CollabSessionResponse;
import com.codesync.collab.model.CollabSession;
import com.codesync.collab.service.CollabService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/collab")
public class CollabController {
    private final CollabService collabService;

    public CollabController(CollabService collabService) {
        this.collabService = collabService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<CollabSessionResponse> createSession(@RequestBody CollabSessionRequest request) {
        CollabSession session = collabService.createSession(request.getFileId(), request.getOwnerId());
        return ResponseEntity.ok(toResponse(session));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<CollabSessionResponse> getSession(@PathVariable String sessionId) {
        return collabService.getSession(sessionId)
                .map(session -> ResponseEntity.ok(toResponse(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sessions/file/{fileId}")
    public ResponseEntity<CollabSessionResponse> getActiveSession(@PathVariable Long fileId) {
        return collabService.getActiveSession(fileId)
                .map(session -> ResponseEntity.ok(toResponse(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<CollabSessionResponse> updateCode(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> payload) {
        CollabSession session = collabService.updateCode(sessionId, payload.get("code"));
        return ResponseEntity.ok(toResponse(session));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> closeSession(@PathVariable String sessionId) {
        collabService.closeSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/collab/{sessionId}/code")
    @SendTo("/topic/collab/{sessionId}")
    public Map<String, Object> handleCodeUpdate(@DestinationVariable String sessionId, Map<String, Object> payload) {
        return payload;
    }

    @MessageMapping("/collab/{sessionId}/cursor")
    @SendTo("/topic/collab/{sessionId}")
    public Map<String, Object> handleCursorUpdate(@DestinationVariable String sessionId, Map<String, Object> payload) {
        return payload;
    }

    private CollabSessionResponse toResponse(CollabSession session) {
        CollabSessionResponse response = new CollabSessionResponse();
        response.setId(session.getId());
        response.setSessionId(session.getSessionId());
        response.setFileId(session.getFileId());
        response.setOwnerId(session.getOwnerId());
        response.setCode(session.getCode());
        response.setIsActive(session.getIsActive());
        response.setCreatedAt(session.getCreatedAt());
        return response;
    }
}