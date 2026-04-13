package com.codesync.collab.service;

import com.codesync.collab.dto.*;
import com.codesync.collab.entity.CollabSession;
import com.codesync.collab.entity.Participant;
import com.codesync.collab.entity.Participant.ParticipantRole;
import com.codesync.collab.feign.FileClient;
import com.codesync.collab.feign.ProjectClient;
import com.codesync.collab.repository.CollabRepository;
import com.codesync.collab.repository.ParticipantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class CollabServiceImpl implements CollabService {

    private final CollabRepository collabRepository;
    private final ParticipantRepository participantRepository;
    private final ProjectClient projectClient;
    private final FileClient fileClient;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String[] COLORS = {
        "#FF5733", "#33FF57", "#3357FF", "#FF33F5", "#33FFF5",
        "#F5FF33", "#FF8833", "#8833FF", "#33FF88", "#FF3388"
    };

    public CollabServiceImpl(
            CollabRepository collabRepository,
            ParticipantRepository participantRepository,
            ProjectClient projectClient,
            FileClient fileClient,
            SimpMessagingTemplate messagingTemplate) {
        this.collabRepository = collabRepository;
        this.participantRepository = participantRepository;
        this.projectClient = projectClient;
        this.fileClient = fileClient;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public CollabSession createSession(CreateSessionRequest request) {
        ResponseEntity<Object> projectResponse = projectClient.getProject(request.getProjectId());
        if (!projectResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Invalid project ID");
        }

        ResponseEntity<Object> fileResponse = fileClient.getFile(request.getFileId());
        if (!fileResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Invalid file ID");
        }

        CollabSession session = CollabSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .projectId(request.getProjectId())
                .fileId(request.getFileId())
                .ownerId(request.getProjectId())
                .status(CollabSession.SessionStatus.ACTIVE)
                .language(request.getLanguage())
                .maxParticipants(request.getMaxParticipants() > 0 ? request.getMaxParticipants() : 10)
                .isPasswordProtected(request.getPassword() != null && !request.getPassword().isEmpty())
                .sessionPassword(request.getPassword())
                .build();

        return collabRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public CollabSession getSessionById(String sessionId) {
        return collabRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollabSession> getSessionsByProject(Long projectId) {
        return collabRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional
    public Participant joinSession(String sessionId, JoinRequest request) {
        CollabSession session = getSessionById(sessionId);

        if (session.getStatus() == CollabSession.SessionStatus.ENDED) {
            throw new IllegalStateException("Session has ended");
        }

        if (session.isPasswordProtected()) {
            if (request.getPassword() == null || !request.getPassword().equals(session.getSessionPassword())) {
                throw new IllegalArgumentException("Invalid password");
            }
        }

        long currentParticipants = participantRepository.countBySessionId(sessionId);
        if (currentParticipants >= session.getMaxParticipants()) {
            throw new IllegalStateException("Session is full");
        }

        Optional<Participant> existingParticipant = participantRepository.findBySessionIdAndUserId(sessionId, request.getUserId());
        if (existingParticipant.isPresent()) {
            return existingParticipant.get();
        }

        ParticipantRole role = request.getRole() != null
                ? ParticipantRole.valueOf(request.getRole().toUpperCase())
                : ParticipantRole.EDITOR;

        Participant participant = Participant.builder()
                .sessionId(sessionId)
                .userId(request.getUserId())
                .role(role)
                .color(assignColor(sessionId))
                .build();

        Participant saved = participantRepository.save(participant);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, Map.of(
                "type", "USER_JOINED",
                "userId", request.getUserId(),
                "color", saved.getColor()
        ));

        return saved;
    }

    @Override
    @Transactional
    public void leaveSession(String sessionId, Long userId) {
        Participant participant = participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not in session"));

        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, Map.of(
                "type", "USER_LEFT",
                "userId", userId
        ));
    }

    @Override
    @Transactional
    public void endSession(String sessionId) {
        CollabSession session = getSessionById(sessionId);
        session.setStatus(CollabSession.SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        collabRepository.save(session);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, Map.of(
                "type", "SESSION_ENDED",
                "sessionId", sessionId
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> getParticipants(String sessionId) {
        return participantRepository.findBySessionId(sessionId);
    }

    @Override
    @Transactional
    public Participant updateCursor(String sessionId, CursorUpdateRequest request) {
        Participant participant = participantRepository.findBySessionIdAndUserId(sessionId, request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not in session"));

        participant.setCursorLine(request.getCursorLine());
        participant.setCursorCol(request.getCursorCol());
        Participant saved = participantRepository.save(participant);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/cursor", Map.of(
                "userId", request.getUserId(),
                "cursorLine", request.getCursorLine(),
                "cursorCol", request.getCursorCol(),
                "color", saved.getColor()
        ));

        return saved;
    }

    @Override
    public void broadcastChange(String sessionId, CodeChangeRequest request) {
        getSessionById(sessionId);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/changes", request);
    }

    @Override
    @Transactional
    public void kickParticipant(String sessionId, Long userId) {
        Participant participant = participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not in session"));

        participantRepository.delete(participant);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, Map.of(
                "type", "USER_KICKED",
                "userId", userId
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CollabSession> getActiveSession(Long fileId) {
        List<CollabSession> sessions = collabRepository.findByFileId(fileId);
        return sessions.stream()
                .filter(s -> s.getStatus() == CollabSession.SessionStatus.ACTIVE)
                .findFirst();
    }

    private String assignColor(String sessionId) {
        List<Participant> participants = participantRepository.findBySessionId(sessionId);
        List<String> usedColors = participants.stream()
                .map(Participant::getColor)
                .toList();

        for (String color : COLORS) {
            if (!usedColors.contains(color)) {
                return color;
            }
        }
        return COLORS[new Random().nextInt(COLORS.length)];
    }
}