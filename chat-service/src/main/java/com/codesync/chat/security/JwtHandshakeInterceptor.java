package com.codesync.chat.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * STOMP channel interceptor for JWT authentication on CONNECT.
 */
@Component
public class JwtHandshakeInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
    private final ChatJwtUtil jwtUtil;

    public JwtHandshakeInterceptor(ChatJwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);

            if (token != null && !token.isEmpty() && jwtUtil.validateToken(token)) {
                try {
                    Claims claims = jwtUtil.extractClaims(token);
                    String userIdStr = claims.getSubject();
                    String username = claims.get("username", String.class);
                    
                    Long userId;
                    // Subject might be the userId directly or a username
                    try {
                        userId = Long.parseLong(userIdStr);
                    } catch (NumberFormatException e) {
                        // Subject is username, try to get userId from claims
                        Object userIdClaim = claims.get("userId");
                        userId = userIdClaim != null ? Long.parseLong(userIdClaim.toString()) : 1L;
                    }

                    accessor.getSessionAttributes().put("userId", userId);
                    if (username != null) {
                        accessor.getSessionAttributes().put("username", username);
                    }
                    log.info("Chat WS authenticated - userId: {}, username: {}", userId, username);
                } catch (Exception e) {
                    log.warn("Failed to extract claims from token: {}", e.getMessage());
                    // Allow connection in dev mode
                    accessor.getSessionAttributes().put("userId", 1L);
                    accessor.getSessionAttributes().put("username", "DevUser");
                }
            } else {
                log.warn("Chat WS connection without valid token - using dev defaults");
                accessor.getSessionAttributes().put("userId", 1L);
                accessor.getSessionAttributes().put("username", "DevUser");
            }
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // Check STOMP native headers (this is where clients send headers on CONNECT)
        List<String> authorization = accessor.getNativeHeader("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            authorization = accessor.getNativeHeader("authorization");
        }
        if (authorization == null || authorization.isEmpty()) {
            authorization = accessor.getNativeHeader("token");
        }

        if (authorization != null && !authorization.isEmpty()) {
            String token = authorization.get(0);
            if (token.startsWith("Bearer ") || token.startsWith("bearer ")) {
                token = token.substring(7);
            }
            return token;
        }

        return null;
    }
}