package com.actify.api.domain.coordination.service;

import com.actify.api.domain.coordination.dto.GuestFeedbackRequest;
import com.actify.api.domain.coordination.dto.GuestProposalResponse;
import com.actify.api.domain.coordination.entity.CoordinationFeedback;
import com.actify.api.domain.coordination.entity.CoordinationSession;
import com.actify.api.domain.coordination.repository.CoordinationFeedbackRepository;
import com.actify.api.domain.coordination.repository.CoordinationSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class IntegrationCoordinationService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationCoordinationService.class);

    private final CoordinationSessionRepository sessionRepository;
    private final CoordinationFeedbackRepository feedbackRepository;

    // 수동 생성자 주입으로 'might not have been initialized' 에러 원천 차단
    public IntegrationCoordinationService(CoordinationSessionRepository sessionRepository, CoordinationFeedbackRepository feedbackRepository) {
        this.sessionRepository = sessionRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public GuestProposalResponse loadGuestSession(String secureToken) {
        CoordinationSession session = sessionRepository.findBySecureToken(secureToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 스케줄 조율 링크입니다."));

        return new GuestProposalResponse(session.getId(), session.getHostName(), session.getCandidateTimeSlots());
    }

    @Transactional
    public void collectAndFinalize(Long coordinationId, GuestFeedbackRequest request) {
        log.info("[Coordination Agent] 조율 방 ID:[{}] 내부 일정 분석 엔진 가동", coordinationId);

        // 1. 순수 자바 표준 반복문으로 피드백 엔티티 변환 및 저장
        List<CoordinationFeedback> feedbacks = new ArrayList<>();
        for (String timeSlot : request.getSelectedTimes()) {
            feedbacks.add(new CoordinationFeedback(
                    null,
                    coordinationId,
                    request.getGuestEmail(),
                    timeSlot
            ));
        }
        feedbackRepository.saveAll(feedbacks);

        // 2. [에이전트 조율 알고리즘 계산 적용]
        CoordinationSession session = sessionRepository.findById(coordinationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조율 세션방입니다."));

        List<CoordinationFeedback> allFeedbacks = feedbackRepository.findByCoordinationId(coordinationId);

        String fixedSchedule = "다수결 합의 시간 불일치로 인한 조율 대기 상태";
        for (CoordinationFeedback feedback : allFeedbacks) {
            String slot = feedback.getSelectedTimeSlot();
            if (session.getCandidateTimeSlots().contains(slot)) {
                fixedSchedule = slot;
                break;
            }
        }

        log.info("[Coordination Agent] 분석 완료. 매칭 성공 스케줄 결과: {}", fixedSchedule);
    }
}
