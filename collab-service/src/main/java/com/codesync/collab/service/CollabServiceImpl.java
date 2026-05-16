package com.codesync.collab.service;
   
import com.codesync.collab.client.FileClient;
import com.codesync.collab.client.NotificationClient;
import com.codesync.collab.client.ProjectClient;
import com.codesync.collab.dto.*;
import com.codesync.collab.exception.SessionCapacityExceededException;
import com.codesync.collab.exception.SessionNotFoundException;
import com.codesync.collab.model.CollabSession;
import com.codesync.collab.model.CollabSession.SessionStatus;
import com.codesync.collab.model.Participant;
import com.codesync.collab.model.Participant.ParticipantRole;
import com.codesync.collab.repository.CollabRepository;
import com.codesync.collab.repository.ParticipantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
   
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
 
@Service
@Transactional
public class CollabServiceImpl implements CollabService {
  
    private static final Logger log = LoggerFactory.getLogger(CollabServiceImpl.class);
  
    private static final String[] COLOR_PALETTE = {
            "#FF5733", "#33A1FF", "#33FF57", "#FF33A1",
            "#A133FF", "#FFD700", "#00CED1", "#FF8C00",
            "#8B008B", "#00FA9A"
    };
 
    private final CollabRepository collabRepository;
    private final ParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationClient notificationClient;
    private final ProjectClient projectClient;
    private final FileClient fileClient;
   
    public CollabServiceImpl(CollabRepository collabRepository,
                             ParticipantRepository participantRepository,
                             SimpMessagingTemplate messagingTemplate,
                             NotificationClient notificationClient,
                             ProjectClient projectClient,
                             FileClient fileClient) {
        this.collabRepository = collabRepository;
        this.participantRepository = participantRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationClient = notificationClient;
        this.projectClient = projectClient;
        this.fileClient = fileClient;
    }
 
    // ── Session lifecycle ──────────────────────────────────────────────────────
 
    @Override
    public CollabSession createSession(CollabSessionRequest request) {
        log.info("=== CREATE SESSION STARTED ===");
        log.info("Request: projectId={}, fileId={}, ownerId={}", 
            request.getProjectId(), request.getFileId(), request.getOwnerId());
        
        try {
            CollabSession session = new CollabSession();
            
            String sessionId = UUID.randomUUID().toString();
            log.info("Generated sessionId: {}", sessionId);
            
            Long projectId = request.getProjectId() != null ? request.getProjectId() : 1L;
            Long fileId = request.getFileId() != null ? request.getFileId() : 1L;
            Long ownerId = request.getOwnerId() != null ? request.getOwnerId() : 1L;

            String projectName = request.getProjectName();
            String fileName = request.getFileName();
            String code = request.getCode();
            
            try {
                var projectResponse = projectClient.getProject(projectId);
                if (projectResponse.getStatusCode().is2xxSuccessful() && projectResponse.getBody() != null) {
                    Map<String, Object> projectData = projectResponse.getBody();
                    projectName = (String) projectData.getOrDefault("name", "Unknown Project");
                    log.info("Fetched project name: {}", projectName);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch project details: {}", e.getMessage());
                if (projectName == null) projectName = "Unknown Project";
            }
            
            try {
                var fileResponse = fileClient.getFileContent(fileId);
                if (fileResponse.getStatusCode().is2xxSuccessful() && fileResponse.getBody() != null) {
                    code = fileResponse.getBody().get("content");
                    log.info("Fetched file content successfully");
                }
            } catch (Exception e) {
                log.warn("Failed to fetch file content: {}", e.getMessage());
                if (code == null) code = "";
            }
            
            try {
                var fileMetaResponse = fileClient.getFile(fileId);
                if (fileMetaResponse.getStatusCode().is2xxSuccessful() && fileMetaResponse.getBody() != null) {
                    Map<String, Object> fileData = fileMetaResponse.getBody();
                    fileName = (String) fileData.getOrDefault("name", fileName != null ? fileName : "Unknown File");
                    String fileLanguage = (String) fileData.get("language");
                    if (fileLanguage != null && session.getLanguage() == null) {
                        request.setLanguage(fileLanguage);
                    }
                    log.info("Fetched file name: {}", fileName);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch file metadata: {}", e.getMessage());
                if (fileName == null) fileName = "Unknown File";
            }
            
            session.setSessionId(sessionId);
            session.setProjectId(projectId);
            session.setFileId(fileId);
            session.setOwnerId(ownerId);
            session.setCode(code != null ? code : "");
            session.setLanguage(request.getLanguage() != null ? request.getLanguage() : "javascript");
            session.setStatus(SessionStatus.ACTIVE);
            session.setMaxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 10);
            session.setIsPasswordProtected(false);
            session.setProjectName(projectName);
            session.setFileName(fileName);
            
            CollabSession saved = collabRepository.save(session);
            
            log.info("Session SAVED with id: {}, sessionId: {}", saved.getId(), saved.getSessionId());
            return saved;
        } catch (Exception e) {
            log.error("ERROR in createSession: {}", e.getMessage(), e);
            throw e;
        }
    }
 
    @Override
    @Transactional(readOnly = true)
    public Optional<CollabSession> getSessionById(String sessionId) {
        return collabRepository.findBySessionId(sessionId);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<CollabSession> getSessionsByProject(Long projectId) {
        return collabRepository.findByProjectId(projectId);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<CollabSession> getSessionsByFile(Long fileId) {
        return collabRepository.findByFileId(fileId);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<CollabSession> getSessionsByOwner(Long ownerId) {
        return collabRepository.findByOwnerId(ownerId);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Optional<CollabSession> getActiveSession(Long fileId) {
        return collabRepository.findActiveByFileId(fileId);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Optional<CollabSession> getActiveSessionByProject(Long projectId) {
        return collabRepository.findActiveByProjectId(projectId);
    }
 
    @Override
    public CollabSession updateCode(String sessionId, String code) {
        CollabSession session = requireActiveSession(sessionId);
        session.setCode(code);
        session.setVersion(session.getVersion() + 1);
        return collabRepository.save(session);
    }
 
    @Override
    public void endSession(String sessionId) {
        CollabSession session = requireSession(sessionId);
        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        collabRepository.save(session);
 
        // Mark all active participants as left
        List<Participant> active = participantRepository.findBySession_SessionIdAndIsActiveTrue(sessionId);
        active.forEach(p -> {
            p.setIsActive(false);
            p.setLeftAt(LocalDateTime.now());
        });
        participantRepository.saveAll(active);

        // Broadcast session ended
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SESSION_ENDED");
        payload.put("sessionId", sessionId);
        payload.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/participants", payload);
    }
 
    // ── Participant management ─────────────────────────────────────────────────
 
    @Override
    public Participant joinSession(String sessionId, JoinSessionRequest request) {
        CollabSession session = requireActiveSession(sessionId);
 
        // Password check
        if (Boolean.TRUE.equals(session.getIsPasswordProtected())
                && request.getRole() != ParticipantRole.HOST) {
            if (request.getSessionPassword() == null
                    || !request.getSessionPassword().equals(session.getSessionPassword())) {
                throw new IllegalArgumentException("Invalid session password");
            }
        }
 
        // Capacity check
        long currentCount = collabRepository.countParticipants(sessionId);
        if (currentCount >= session.getMaxParticipants()) {
            throw new SessionCapacityExceededException(
                    "Session is full (" + session.getMaxParticipants() + " participants max)");
        }
 
        // Re-join if participant already exists but left
        Optional<Participant> existing = participantRepository
                .findBySession_SessionIdAndUserId(sessionId, request.getUserId());
        if (existing.isPresent()) {
            Participant p = existing.get();
            p.setIsActive(true);
            p.setLeftAt(null);
            p.setJoinedAt(LocalDateTime.now());
            Participant saved = participantRepository.save(p);
            broadcastParticipantEvent(sessionId, saved, "PARTICIPANT_JOINED");
            return saved;
        }
 
        // Assign next available colour
        String color = assignColor(sessionId);
 
        Participant participant = new Participant();
        participant.setSession(session);
        participant.setUserId(request.getUserId());
        participant.setRole(request.getRole() != null ? request.getRole() : ParticipantRole.VIEWER);
        participant.setColor(color);
        participant.setCursorLine(0);
        participant.setCursorCol(0);
        participant.setIsActive(true);
 
        Participant saved = participantRepository.save(participant);
        broadcastParticipantEvent(sessionId, saved, "PARTICIPANT_JOINED");
        return saved;
    }
 
    @Override
    public void leaveSession(String sessionId, Long userId) {
        requireSession(sessionId);
        Participant participant = participantRepository
                .findBySession_SessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Participant " + userId + " not found in session " + sessionId));
        participant.setIsActive(false);
        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);
        broadcastParticipantEvent(sessionId, participant, "PARTICIPANT_LEFT");
    }
 
    @Override
    public void kickParticipant(String sessionId, Long requesterId, Long targetUserId) {
        CollabSession session = requireActiveSession(sessionId);
 
        if (!session.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("Only the session owner can kick participants");
        }
 
        Participant target = participantRepository
                .findBySession_SessionIdAndUserId(sessionId, targetUserId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Participant " + targetUserId + " not found in session " + sessionId));
 
        target.setIsActive(false);
        target.setLeftAt(LocalDateTime.now());
        participantRepository.save(target);
        broadcastParticipantEvent(sessionId, target, "PARTICIPANT_KICKED");
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<Participant> getParticipants(String sessionId) {
        requireSession(sessionId);
        return participantRepository.findBySession_SessionIdAndIsActiveTrue(sessionId);
    }
 
    // ── Real-time cursor ───────────────────────────────────────────────────────
 
    @Override
    public Participant updateCursor(String sessionId, UpdateCursorRequest request) {
        requireActiveSession(sessionId);
        Participant participant = participantRepository
                .findBySession_SessionIdAndUserId(sessionId, request.getUserId())
                .orElseThrow(() -> new SessionNotFoundException(
                        "Participant " + request.getUserId() + " not found in session " + sessionId));
        participant.setCursorLine(request.getCursorLine());
        participant.setCursorCol(request.getCursorCol());
        Participant saved = participantRepository.save(participant);
 
        // Broadcast cursor move — UNIFIED TOPIC
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/cursors", Map.of(
                "type", "CURSOR_UPDATE",
                "sessionId", sessionId,
                "userId", request.getUserId(),
                "color", participant.getColor(),
                "cursorLine", request.getCursorLine(),
                "cursorCol", request.getCursorCol()
        ));
 
        return saved;
    }
 
    // ── WebSocket broadcast ────────────────────────────────────────────────────
 
    @Override
    public void broadcastChange(String sessionId, Object payload) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/code", payload);
    }

    @Override
    public void broadcastEvent(String sessionId, Map<String, Object> event) {
        log.info("Broadcasting event to session {}: {}", sessionId, event.get("type"));
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/comments", event);
    }
 
    // ── Private helpers ────────────────────────────────────────────────────────
 
    private CollabSession requireSession(String sessionId) {
        return collabRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
    }
 
    private CollabSession requireActiveSession(String sessionId) {
        CollabSession session = requireSession(sessionId);
        if (session.getStatus() == SessionStatus.ENDED) {
            throw new IllegalStateException("Session " + sessionId + " has already ended");
        }
        return session;
    }
 
    private String assignColor(String sessionId) {
        long count = collabRepository.countParticipants(sessionId);
        return COLOR_PALETTE[(int) (count % COLOR_PALETTE.length)];
    }
 
    /**
     * Broadcast participant events — UNIFIED TOPIC: /topic/session/{id}/participants
     */
    private void broadcastParticipantEvent(String sessionId, Participant participant, String eventType) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/participants", Map.of(
                "type", eventType,
                "sessionId", sessionId,
                "userId", participant.getUserId(),
                "color", participant.getColor() != null ? participant.getColor() : "",
                "role", participant.getRole().name()
        ));
    }

    private void sendSessionNotification(Long recipientId, Long actorId, String sessionId, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("recipientId", recipientId);
            notification.put("actorId", actorId);
            notification.put("type", "SESSION_INVITE");
            notification.put("title", "Session Invitation");
            notification.put("message", message);
            notification.put("relatedId", sessionId);
            notification.put("relatedType", "SESSION");
            notification.put("sendEmail", false);
            notificationClient.createNotification(notification);
            log.info("Sent session notification to user: {}", recipientId);
        } catch (Exception e) {
            log.warn("Failed to send session notification: {}", e.getMessage());
        }
    }
}