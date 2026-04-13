package com.codesync.collab.controller;

import com.codesync.collab.dto.CodeChangeRequest;
import com.codesync.collab.dto.CursorUpdateRequest;
import com.codesync.collab.service.CollabService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class CollabWebSocketController {

    private final CollabService collabService;

    public CollabWebSocketController(CollabService collabService) {
        this.collabService = collabService;
    }

    @MessageMapping("/change/{sessionId}")
    @SendTo("/topic/session/{sessionId}/changes")
    public CodeChangeRequest broadcastChange(
            @DestinationVariable String sessionId,
            CodeChangeRequest request) {
        collabService.broadcastChange(sessionId, request);
        return request;
    }

    @MessageMapping("/cursor/{sessionId}")
    @SendTo("/topic/session/{sessionId}/cursor")
    public CursorUpdateRequest updateCursor(
            @DestinationVariable String sessionId,
            CursorUpdateRequest request) {
        collabService.updateCursor(sessionId, request);
        return request;
    }
}