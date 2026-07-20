package com.codesync.collab.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisMessageRelay implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisMessageRelay.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final ChannelTopic collabTopic;

    public void publish(String destination, Object payload) {
        try {
            Map<String, Object> wrapper = Map.of(
                    "destination", destination,
                    "payload", payload
            );
            redisTemplate.convertAndSend(collabTopic.getTopic(), wrapper);
        } catch (Exception e) {
            log.error("Failed to publish to Redis, falling back to local: {}", e.getMessage());
            messagingTemplate.convertAndSend(destination, payload);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Map<String, Object> wrapper = objectMapper.readValue(
                    message.getBody(), new TypeReference<>() {});
            String destination = (String) wrapper.get("destination");
            Object payload = wrapper.get("payload");
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            log.error("Failed to relay Redis message to WebSocket: {}", e.getMessage());
        }
    }
}
