package com.codesync.collab.repository;
  
import com.codesync.collab.model.CollabSession;
import com.codesync.collab.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
  
import java.util.List;
import java.util.Optional;
  
public interface CollabRepository extends JpaRepository<CollabSession, Long> {
  
    Optional<CollabSession> findBySessionId(String sessionId);
  
    List<CollabSession> findByProjectId(Long projectId);
  
    List<CollabSession> findByFileId(Long fileId);
  
    List<CollabSession> findByOwnerId(Long ownerId);
  
    @Query("SELECT s FROM CollabSession s WHERE s.projectId = :projectId AND s.status = 'ACTIVE'")
    Optional<CollabSession> findActiveByProjectId(@Param("projectId") Long projectId);
   
    @Query("SELECT s FROM CollabSession s WHERE s.fileId = :fileId AND s.status = 'ACTIVE'")
    Optional<CollabSession> findActiveByFileId(@Param("fileId") Long fileId);
   
    @Query("SELECT p FROM Participant p WHERE p.session.sessionId = :sessionId AND p.isActive = true")
    List<Participant> findParticipantsBySessionId(@Param("sessionId") String sessionId);
   
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.session.sessionId = :sessionId AND p.isActive = true")
    long countParticipants(@Param("sessionId") String sessionId);
  
    @Query("SELECT s FROM CollabSession s WHERE s.sessionId = :sessionId AND s.status = 'ACTIVE'")
    Optional<CollabSession> findActiveBySessionId(@Param("sessionId") String sessionId);
}