package com.codesync.collab.repository;
 
import com.codesync.collab.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
 
    Optional<Participant> findBySession_SessionIdAndUserId(String sessionId, Long userId);
 
    List<Participant> findBySession_SessionIdAndIsActiveTrue(String sessionId);
}
 