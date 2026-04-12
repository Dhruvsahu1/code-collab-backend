package com.codesync.collab.repository;

import com.codesync.collab.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findBySessionId(String sessionId);

    Optional<Participant> findBySessionIdAndUserId(String sessionId, Long userId);

    long countBySessionId(String sessionId);
}