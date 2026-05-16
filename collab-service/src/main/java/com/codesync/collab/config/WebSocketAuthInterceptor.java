package com.codesync.collab.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            
            if (authorization == null || authorization.isEmpty()) {
                authorization = accessor.getNativeHeader("authorization");
            }
            
            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.get(0);
                
                if (token.startsWith("Bearer ") || token.startsWith("bearer ")) {
                    token = token.substring(7);
                }
                
                try {
                    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
                    if (keyBytes.length < 32) {
                        byte[] paddedKey = new byte[32];
                        System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
                        keyBytes = paddedKey;
                    }
                    SecretKey key = Keys.hmacShaKeyFor(keyBytes);
                    
                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
                    
                    Long userId = claims.get("userId", Long.class);
                    String role = claims.get("role", String.class);
                    
                    if (userId != null) {
                        accessor.getSessionAttributes().put("userId", userId);
                        if (role != null) {
                            accessor.getSessionAttributes().put("role", role);
                        }
                    }
                } catch (Exception e) {
                    // Auth failed, but allow connection for development
                    accessor.getSessionAttributes().put("userId", 1L);
                }
            } else {
                // No auth header - allow for development
                accessor.getSessionAttributes().put("userId", 1L);
            }
        }
        
        return message;
    }
}