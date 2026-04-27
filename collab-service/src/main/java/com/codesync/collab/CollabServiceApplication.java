package com.codesync.collab;

import com.codesync.collab.client.FileClient;
import com.codesync.collab.client.NotificationClient;
import com.codesync.collab.client.ProjectClient;
import com.codesync.collab.controller.CollabController;
import com.codesync.collab.controller.CollabWebSocketController;
import com.codesync.collab.repository.CollabRepository;
import com.codesync.collab.repository.ParticipantRepository;
import com.codesync.collab.service.CollabService;
import com.codesync.collab.service.CollabServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
@SpringBootApplication
@EnableFeignClients
public class CollabServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(CollabServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CollabServiceApplication.class, args);
    }

    @Bean
    public CollabService collabService(CollabRepository collabRepo, ParticipantRepository participantRepo, 
            SimpMessagingTemplate messagingTemplate, NotificationClient notificationClient,
            ProjectClient projectClient, FileClient fileClient) {
        log.info(">>> CREATING CollabService BEAN <<<");
        return new CollabServiceImpl(collabRepo, participantRepo, messagingTemplate, notificationClient, projectClient, fileClient);
    }

    @Bean
    public CollabController collabController(CollabService collabService) {
        log.info(">>> CREATING CollabController BEAN <<<");
        return new CollabController(collabService);
    }

    @Bean
    public CollabWebSocketController collabWebSocketController(CollabService collabService, SimpMessagingTemplate messagingTemplate) {
        log.info(">>> CREATING CollabWebSocketController BEAN <<<");
        return new CollabWebSocketController(collabService, messagingTemplate);
    }
}