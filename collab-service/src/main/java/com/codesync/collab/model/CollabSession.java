package com.codesync.collab.model;
 
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
@Entity
@Table(name = "collab_sessions")
@Getter
@Setter
@NoArgsConstructor
public class CollabSession {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String sessionId;
 
    @Column(nullable = false)
    private Long projectId;
 
    @Column(nullable = false)
    private Long fileId;
 
    @Column(nullable = false)
    private Long ownerId;
 
    @Column(columnDefinition = "TEXT")
    private String code;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;
 
@Column
    private String language;

@Column(nullable = true)
    private Integer version = 0;

    @Column
    private String sessionPassword;

    @Column
    private Integer maxParticipants = 10;

    @Column(nullable = true)
    private Boolean isPasswordProtected = false;

    @Column
    private String projectName;

    @Column
    private String fileName;
  
    @Column(nullable = true)
    private LocalDateTime createdAt;
 
    @Column
    private LocalDateTime updatedAt;
 
    @Column
    private LocalDateTime endedAt;
 
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Participant> participants = new ArrayList<>();
 
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (version == null) version = 0;
        if (status == null) status = SessionStatus.ACTIVE;
        if (maxParticipants == null) maxParticipants = 10;
        if (isPasswordProtected == null) isPasswordProtected = false;
    }
 
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
 
    public enum SessionStatus {
        ACTIVE, ENDED
    }
}