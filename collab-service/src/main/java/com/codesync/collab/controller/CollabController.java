package com.codesync.collab.controller;

import com.codesync.collab.dto.*;
import com.codesync.collab.entity.CollabSession;
import com.codesync.collab.entity.Participant;
import com.codesync.collab.service.CollabService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
public class CollabController {

    private final CollabService collabService;

    public CollabController(CollabService collabService) {
        this.collabService = collabService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        CollabSession session = collabService.createSession(request);
        return ResponseEntity.ok(SessionResponse.fromEntity(session));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable String sessionId) {
        CollabSession session = collabService.getSessionById(sessionId);
        return ResponseEntity.ok(SessionResponse.fromEntity(session));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByProject(@PathVariable Long projectId) {
        List<CollabSession> sessions = collabService.getSessionsByProject(projectId);
        List<SessionResponse> responses = sessions.stream()
                .map(SessionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/join/{sessionId}")
    public ResponseEntity<ParticipantResponse> joinSession(
            @PathVariable String sessionId,
            @RequestBody JoinRequest request) {
        Participant participant = collabService.joinSession(sessionId, request);
        return ResponseEntity.ok(ParticipantResponse.fromEntity(participant));
    }

    @PostMapping("/leave/{sessionId}")
    public ResponseEntity<Void> leaveSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        collabService.leaveSession(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/end/{sessionId}")
    public ResponseEntity<Void> endSession(@PathVariable String sessionId) {
        collabService.endSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/participants/{sessionId}")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable String sessionId) {
        List<Participant> participants = collabService.getParticipants(sessionId);
        List<ParticipantResponse> responses = participants.stream()
                .map(ParticipantResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/cursor/{sessionId}")
    public ResponseEntity<ParticipantResponse> updateCursor(
            @PathVariable String sessionId,
            @RequestBody CursorUpdateRequest request) {
        Participant participant = collabService.updateCursor(sessionId, request);
        return ResponseEntity.ok(ParticipantResponse.fromEntity(participant));
    }

    @PostMapping("/kick/{sessionId}")
    public ResponseEntity<Void> kickParticipant(
            @PathVariable String sessionId,
            @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        collabService.kickParticipant(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active/{fileId}")
    public ResponseEntity<SessionResponse> getActiveSession(@PathVariable Long fileId) {
        return collabService.getActiveSession(fileId)
                .map(session -> ResponseEntity.ok(SessionResponse.fromEntity(session)))
                .orElse(ResponseEntity.notFound().build());
    }
}