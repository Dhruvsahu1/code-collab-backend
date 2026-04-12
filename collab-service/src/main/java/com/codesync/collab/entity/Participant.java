package com.codesync.collab.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role", length = 20)
    @Enumerated(EnumType.STRING)
    private ParticipantRole role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "cursor_line")
    private int cursorLine;

    @Column(name = "cursor_col")
    private int cursorCol;

    @Column(name = "color", length = 7)
    private String color;

    public enum ParticipantRole {
        HOST,
        EDITOR,
        VIEWER
    }

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (role == null) {
            role = ParticipantRole.EDITOR;
        }
    }
}