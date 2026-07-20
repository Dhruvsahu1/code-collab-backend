package com.codesync.collab.repository;

import com.codesync.collab.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findBySession_SessionId(String sessionId);

    Optional<Participant> findBySession_SessionIdAndUserId(String sessionId, Long userId);

    List<Participant> findBySession_SessionIdAndIsActiveTrue(String sessionId);
}
