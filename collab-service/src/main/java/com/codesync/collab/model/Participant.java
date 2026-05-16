package com.codesync.collab.model;
 
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "collab_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Participant {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CollabSession session;
 
    @Column(nullable = false)
    private Long userId;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role = ParticipantRole.VIEWER;
 
    @Column(nullable = false)
    private LocalDateTime joinedAt;
 
    @Column
    private LocalDateTime leftAt;
 
    /** Live cursor position */
    @Column
    private Integer cursorLine = 0;
 
    @Column
    private Integer cursorCol = 0;
 
    /** Unique colour assigned per session (hex string, e.g. "#FF5733") */
    @Column(length = 7)
    private String color;
 
    @Column(nullable = false)
    private Boolean isActive = true;
 
    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
 
    public enum ParticipantRole {
        HOST, EDITOR, VIEWER
    }
}