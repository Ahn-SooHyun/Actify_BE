package com.actify.api.domain.coordination.repository;

import com.actify.api.domain.coordination.entity.CoordinationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CoordinationSessionRepository extends JpaRepository<CoordinationSession, Long> {
    Optional<CoordinationSession> findBySecureToken(String secureToken);
}