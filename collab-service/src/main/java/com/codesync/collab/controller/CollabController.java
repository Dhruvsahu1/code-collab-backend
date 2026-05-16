package com.codesync.collab.controller;
 
import com.codesync.collab.dto.*;
import com.codesync.collab.model.CollabSession;
import com.codesync.collab.model.Participant;
import com.codesync.collab.service.CollabService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
  
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
@RestController
@RequestMapping("/api/sessions")
public class CollabController {
  
    private final CollabService collabService;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CollabController.class);
  
    public CollabController(CollabService collabService) {
        this.collabService = collabService;
    }
  
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info(">>> PING ENDPOINT HIT <<<");
        return ResponseEntity.ok("collab-service is running!");
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info(">>> TEST ENDPOINT HIT <<<");
        return ResponseEntity.ok("Collab Service Working at /api/sessions/test");
    }
    
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint(@RequestBody Map<String, Object> request) {
        log.info("TEST ENDPOINT RECEIVED: {}", request);
        return ResponseEntity.ok(request);
    }
  
    // ── Session CRUD (existing routes preserved) ───────────────────────────────
 
    /**
     * POST /api/sessions
     * Create a new collaboration session.
     */
    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody Map<String, Object> request) {
        log.info("RECEIVED REQUEST BODY: {}", request);
        
        // Extract values from Map (safe approach)
        Long projectId = request.get("projectId") != null ? Long.parseLong(request.get("projectId").toString()) : 1L;
        Long fileId = request.get("fileId") != null ? Long.parseLong(request.get("fileId").toString()) : 1L;
        Long ownerId = request.get("ownerId") != null ? Long.parseLong(request.get("ownerId").toString()) : 1L;
        
        log.info("EXTRACTED - projectId: {}, fileId: {}, ownerId: {}", projectId, fileId, ownerId);
        
        // Create DTO from Map values
        CollabSessionRequest dto = new CollabSessionRequest();
        dto.setProjectId(projectId);
        dto.setFileId(fileId);
        dto.setOwnerId(ownerId);
        if (request.get("language") != null) {
            dto.setLanguage(request.get("language").toString());
        }
        if (request.get("maxParticipants") != null) {
            dto.setMaxParticipants(Integer.parseInt(request.get("maxParticipants").toString()));
        }
        if (request.get("projectName") != null) {
            dto.setProjectName(request.get("projectName").toString());
        }
        if (request.get("fileName") != null) {
            dto.setFileName(request.get("fileName").toString());
        }
        
        CollabSession session = collabService.createSession(dto);
        return ResponseEntity.status(201).body(toResponse(session));
    }
 
    /**
     * GET /collab/sessions/{sessionId}
     * Fetch a session by its UUID session-id.
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<CollabSessionResponse> getSession(@PathVariable String sessionId) {
        return collabService.getSessionById(sessionId)
                .map(s -> ResponseEntity.ok(toResponse(s)))
                .orElse(ResponseEntity.notFound().build());
    }
 
    /**
     * GET /api/sessions/file/{fileId}
     * Fetch the currently ACTIVE session for a given file.
     */
    @GetMapping("/file/{fileId}")
    public ResponseEntity<CollabSessionResponse> getActiveSession(@PathVariable Long fileId) {
        return collabService.getActiveSession(fileId)
                .map(s -> ResponseEntity.ok(toResponse(s)))
                .orElse(ResponseEntity.notFound().build());
    }
 
    /**
     * GET /collab/sessions/project/{projectId}
     * All sessions for a project.
     */
@GetMapping("/project/{projectId}")
    public ResponseEntity<List<CollabSessionResponse>> getSessionsByProject(@PathVariable Long projectId) {
        List<CollabSessionResponse> list = collabService.getSessionsByProject(projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/sessions/project/{projectId}/active
     * The single active session for a project.
     */
    @GetMapping("/project/{projectId}/active")
    public ResponseEntity<CollabSessionResponse> getActiveSessionByProject(@PathVariable Long projectId) {
        return collabService.getActiveSessionByProject(projectId)
                .map(s -> ResponseEntity.ok(toResponse(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/sessions/owner/{ownerId}
     * All sessions owned by a user.
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<CollabSessionResponse>> getSessionsByOwner(@PathVariable Long ownerId) {
        List<CollabSessionResponse> list = collabService.getSessionsByOwner(ownerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * PUT /api/sessions/{sessionId}
     * Update the code snapshot in a session (HTTP fallback; WebSocket is preferred for live edits).
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<CollabSessionResponse> updateCode(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> payload) {
        CollabSession session = collabService.updateCode(sessionId, payload.get("code"));
        return ResponseEntity.ok(toResponse(session));
    }

    /**
     * DELETE /api/sessions/{sessionId}
     * End / close a session (was closeSession before — same URL, corrected semantics).
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> closeSession(@PathVariable String sessionId) {
        collabService.endSession(sessionId);
        return ResponseEntity.ok().build();
    }

    // ── Participant management ─────────────────────────────────────────────────

    /**
     * POST /api/sessions/{sessionId}/join
     * A user joins an existing active session.
     */
    @PostMapping("/{sessionId}/join")
    public ResponseEntity<ParticipantResponse> joinSession(
            @PathVariable String sessionId,
            @RequestBody JoinSessionRequest request) {
        Participant participant = collabService.joinSession(sessionId, request);
        return ResponseEntity.ok(toParticipantResponse(participant));
    }

    /**
     * POST /api/sessions/{sessionId}/leave
     * A user leaves a session voluntarily.
     * Body: { "userId": 123 }
     */
    @PostMapping("/{sessionId}/leave")
    public ResponseEntity<Void> leaveSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, Long> payload) {
        collabService.leaveSession(sessionId, payload.get("userId"));
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/sessions/{sessionId}/end
     * Owner explicitly ends the session (sets status = ENDED).
     */
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<Void> endSession(@PathVariable String sessionId) {
        collabService.endSession(sessionId);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/sessions/{sessionId}/kick
     * Owner kicks a participant.
     * Body: { "requesterId": 1, "targetUserId": 2 }
     */
    @PostMapping("/{sessionId}/kick")
    public ResponseEntity<Void> kickParticipant(
            @PathVariable String sessionId,
            @RequestBody Map<String, Long> payload) {
        collabService.kickParticipant(sessionId, payload.get("requesterId"), payload.get("targetUserId"));
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/sessions/{sessionId}/participants
     * List all currently active participants.
     */
    @GetMapping("/{sessionId}/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable String sessionId) {
        List<ParticipantResponse> list = collabService.getParticipants(sessionId)
                .stream().map(this::toParticipantResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * PUT /api/sessions/{sessionId}/cursor
     * Update a participant's cursor position via HTTP (WebSocket is preferred).
     */
@PutMapping("/{sessionId}/cursor")
    public ResponseEntity<ParticipantResponse> updateCursor(
            @PathVariable String sessionId,
            @RequestBody UpdateCursorRequest request) {
        Participant participant = collabService.updateCursor(sessionId, request);
        return ResponseEntity.ok(toParticipantResponse(participant));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcastEvent(@RequestBody Map<String, Object> event) {
        log.info("BROADCAST EVENT RECEIVED: {}", event);
        String type = (String) event.get("type");
        String sessionId = (String) event.get("sessionId");
        
        if (sessionId != null) {
            collabService.broadcastEvent(sessionId, event);
        }
        
        return ResponseEntity.ok(Map.of("status", "broadcasted"));
    }
  
    // ── Mappers ────────────────────────────────────────────────────────────────
 
    private CollabSessionResponse toResponse(CollabSession session) {
        CollabSessionResponse response = new CollabSessionResponse();
        response.setId(session.getId());
        response.setSessionId(session.getSessionId());
        response.setProjectId(session.getProjectId());
        response.setFileId(session.getFileId());
        response.setOwnerId(session.getOwnerId());
        response.setCode(session.getCode());
        response.setStatus(session.getStatus());
        response.setLanguage(session.getLanguage());
        response.setMaxParticipants(session.getMaxParticipants());
        response.setIsPasswordProtected(session.getIsPasswordProtected());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        response.setEndedAt(session.getEndedAt());
        response.setProjectName(session.getProjectName());
        response.setFileName(session.getFileName());
        return response;
    }
 
    private ParticipantResponse toParticipantResponse(Participant p) {
        ParticipantResponse response = new ParticipantResponse();
        response.setParticipantId(p.getParticipantId());
        response.setUserId(p.getUserId());
        response.setRole(p.getRole());
        response.setColor(p.getColor());
        response.setCursorLine(p.getCursorLine());
        response.setCursorCol(p.getCursorCol());
        response.setJoinedAt(p.getJoinedAt());
        response.setLeftAt(p.getLeftAt());
        response.setIsActive(p.getIsActive());
        return response;
    }
}