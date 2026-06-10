package com.actify.api.domain.coordination.repository;

import com.actify.api.domain.coordination.entity.CoordinationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoordinationFeedbackRepository extends JpaRepository<CoordinationFeedback, Long> {
    List<CoordinationFeedback> findByCoordinationId(Long coordinationId);
}