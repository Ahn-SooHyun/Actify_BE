package co.kr.demo.service.impl;

import co.kr.demo.exception.CoordinationFailedException;
import co.kr.demo.model.dto.request.CoordinationReq;
import co.kr.demo.model.dto.response.CoordinationRes;
import co.kr.demo.model.entity.CoordinationRecipientEntity;
import co.kr.demo.model.entity.CoordinationRequestEntity;
import co.kr.demo.model.entity.ScheduleEntity;
import co.kr.demo.model.vo.SlotCandidate;
import co.kr.demo.model.vo.enums.RecipientInviteStatus;
import co.kr.demo.model.vo.enums.ScheduleSource;
import co.kr.demo.model.vo.enums.ScheduleStatus;
import co.kr.demo.repository.CoordinationRecipientRepository;
import co.kr.demo.repository.CoordinationRequestRepository;
import co.kr.demo.repository.ScheduleRepository;
import co.kr.demo.service.CoordinationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 회원 간 일정 조율 구현체.
 *
 * 책임:
 *  - 조율 요청 처리 (coordinate)
 *  - 참여자의 수락 응답 처리 (accept) — 양측 모두 ACCEPT 시 조율 CONFIRMED
 *  - 참여자의 거절 응답 처리 (decline) — 즉시 양측 모두 CANCELLED
 *
 * 짝 매칭 전략:
 *  Coordination_Requests의 Final_Start_Time/End_Time과
 *  Coordination_Recipients의 사용자 ID 조합으로 PROPOSED 일정을 찾아
 *  검수 동기화를 처리한다 (스키마 변경 없는 동적 매칭).
 */
@Service
public class CoordinationServiceImpl implements CoordinationService {

    private static final int SLOT_GRANULARITY_MINUTES = 30;

    private final ScheduleRepository scheduleRepository;
    private final CoordinationRequestRepository coordinationRequestRepository;
    private final CoordinationRecipientRepository coordinationRecipientRepository;

    public CoordinationServiceImpl(
            ScheduleRepository scheduleRepository,
            CoordinationRequestRepository coordinationRequestRepository,
            CoordinationRecipientRepository coordinationRecipientRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.coordinationRequestRepository = coordinationRequestRepository;
        this.coordinationRecipientRepository = coordinationRecipientRepository;
    }

    // ---------- 조율 요청 처리 ----------

    @Override
    @Transactional
    public CoordinationRes coordinate(CoordinationReq req) {
        // 1. 입력 검증
        validateRequest(req);

        // 2. 두 사용자의 기간 내 일정 조회
        List<ScheduleEntity> userASchedules = scheduleRepository
                .findByUsersIdxAndDelFalseAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
                        req.userAIdx(), req.searchFrom(), req.searchTo());
        List<ScheduleEntity> userBSchedules = scheduleRepository
                .findByUsersIdxAndDelFalseAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
                        req.userBIdx(), req.searchFrom(), req.searchTo());

        // 3~4. 슬롯 후보 생성 + 양쪽 가능 슬롯만 필터링
        List<SlotCandidate> availableSlots = generateAvailableSlots(
                req.searchFrom(),
                req.searchTo(),
                req.durationMinutes(),
                userASchedules,
                userBSchedules
        );

        if (availableSlots.isEmpty()) {
            throw new CoordinationFailedException(
                    "검색 기간 내 두 사용자 모두 가능한 슬롯을 찾을 수 없습니다");
        }

        // 5~6. 점수 매겨 최고점 선택
        SlotCandidate chosen = availableSlots.stream()
                .max(Comparator.comparingDouble(SlotCandidate::score))
                .orElseThrow();

        // 7. Coordination_Requests에 마스터 행 저장 (NEGOTIATING)
        CoordinationRequestEntity request = new CoordinationRequestEntity(
                req.userAIdx(),
                UUID.randomUUID().toString(),
                req.title(),
                chosen.start(),
                chosen.end()
        );
        if (req.description() != null) request.setDescription(req.description());
        if (req.category() != null) request.setCategory(req.category());
        CoordinationRequestEntity savedRequest =
                coordinationRequestRepository.save(request);

        // 8. Coordination_Recipients에 참여자 두 명 저장
        saveRecipient(savedRequest.getId(), req.userAIdx());
        saveRecipient(savedRequest.getId(), req.userBIdx());

        // 9. 양측 캘린더에 PROPOSED 일정 저장
        ScheduleEntity userASaved = saveProposedSchedule(req, chosen, req.userAIdx());
        ScheduleEntity userBSaved = saveProposedSchedule(req, chosen, req.userBIdx());

        // 10. 결과 DTO 변환
        return CoordinationRes.from(savedRequest, userASaved, userBSaved, chosen.score());
    }

    // ---------- 수락 처리 ----------

    @Override
    @Transactional
    public void accept(Long coordinationId, Long usersIdx) {
        // 1. 해당 참여자 찾기
        CoordinationRecipientEntity recipient = coordinationRecipientRepository
                .findByCoordinationIdxAndRecipientUsersIdxAndDelFalse(
                        coordinationId, usersIdx)
                .orElseThrow(() -> new CoordinationFailedException(
                        "해당 조율의 참여자가 아닙니다"));

        // 2. 이 참여자를 ACCEPTED로 표시
        recipient.accept();

        // 3. 모든 참여자가 ACCEPTED인지 확인
        List<CoordinationRecipientEntity> allRecipients = coordinationRecipientRepository
                .findByCoordinationIdxAndDelFalse(coordinationId);
        boolean allAccepted = allRecipients.stream()
                .allMatch(r -> r.getInviteStatus() == RecipientInviteStatus.ACCEPTED);

        // 4. 모두 ACCEPTED면 양측 일정과 조율 요청을 CONFIRMED로 전환
        if (allAccepted) {
            CoordinationRequestEntity request = loadRequest(coordinationId);
            confirmAllSchedules(request, allRecipients);
            request.confirm();
        }
        // 일부만 ACCEPTED면 NEGOTIATING 유지 (별도 처리 없음)
    }

    // ---------- 거절 처리 ----------

    @Override
    @Transactional
    public void decline(Long coordinationId, Long usersIdx) {
        // 1. 해당 참여자 찾기
        CoordinationRecipientEntity recipient = coordinationRecipientRepository
                .findByCoordinationIdxAndRecipientUsersIdxAndDelFalse(
                        coordinationId, usersIdx)
                .orElseThrow(() -> new CoordinationFailedException(
                        "해당 조율의 참여자가 아닙니다"));

        // 2. 이 참여자를 DECLINED로 표시
        recipient.decline();

        // 3. 한 명이라도 거절하면 즉시 양측 모두 취소
        CoordinationRequestEntity request = loadRequest(coordinationId);
        List<CoordinationRecipientEntity> allRecipients = coordinationRecipientRepository
                .findByCoordinationIdxAndDelFalse(coordinationId);
        cancelAllSchedules(request, allRecipients);
        request.cancel();
    }

    // ---------- 검수 동기화 헬퍼 ----------

    /**
     * 양측 모두 ACCEPTED일 때 양측 PROPOSED 일정을 CONFIRMED로 전환.
     * Final 시각 + 참여자 사용자 ID + AGENT_NEGOTIATION 출처로 짝 일정을 찾는다.
     */
    private void confirmAllSchedules(
            CoordinationRequestEntity request,
            List<CoordinationRecipientEntity> recipients
    ) {
        for (CoordinationRecipientEntity recipient : recipients) {
            if (recipient.getRecipientUsersIdx() == null) continue;   // 비회원 건너뜀
            ScheduleEntity schedule = findPairedSchedule(request, recipient.getRecipientUsersIdx());
            if (schedule != null) {
                schedule.confirm();   // status=CONFIRMED, confirmedAt=now
            }
        }
    }

    /**
     * 한쪽이라도 거절했을 때 양측 PROPOSED 일정을 모두 CANCELLED로 전환.
     */
    private void cancelAllSchedules(
            CoordinationRequestEntity request,
            List<CoordinationRecipientEntity> recipients
    ) {
        for (CoordinationRecipientEntity recipient : recipients) {
            if (recipient.getRecipientUsersIdx() == null) continue;
            ScheduleEntity schedule = findPairedSchedule(request, recipient.getRecipientUsersIdx());
            if (schedule != null && schedule.getStatus() == ScheduleStatus.PROPOSED) {
                schedule.setStatus(ScheduleStatus.CANCELLED);
            }
        }
    }

    /**
     * 동적 짝 매칭 — 시각 + 사용자 + 출처 + 상태로 PROPOSED 일정을 찾는다.
     * 스키마 변경 없이 두 일정의 짝 관계를 표현하는 핵심 로직.
     */
    private ScheduleEntity findPairedSchedule(
            CoordinationRequestEntity request,
            Long usersIdx
    ) {
        Instant start = request.getFinalStartTime();
        Instant end = request.getFinalEndTime();
        // 해당 사용자의 일정 중 시작 시각이 정확히 일치하고 출처가 조율인 것
        return scheduleRepository
                .findByUsersIdxAndDelFalseAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
                        usersIdx, start, start.plusSeconds(1))
                .stream()
                .filter(s -> s.getEndTime().equals(end))
                .filter(s -> s.getSource() == ScheduleSource.AGENT_NEGOTIATION)
                .findFirst()
                .orElse(null);
    }

    // ---------- 저장 헬퍼 ----------

    private void saveRecipient(Long coordinationId, Long usersIdx) {
        CoordinationRecipientEntity recipient = new CoordinationRecipientEntity(
                coordinationId,
                usersIdx,
                "User-" + usersIdx   // 표시명. 향후 Users 테이블에서 실제 이름 조회 가능
        );
        coordinationRecipientRepository.save(recipient);
    }

    private ScheduleEntity saveProposedSchedule(
            CoordinationReq req,
            SlotCandidate slot,
            Long usersIdx
    ) {
        ScheduleEntity s = new ScheduleEntity(
                usersIdx,
                req.title(),
                slot.start(),
                slot.end()
        );
        if (req.category() != null) s.setCategory(req.category());
        if (req.address() != null) s.setAddress(req.address());
        if (req.description() != null) s.setDescription(req.description());

        s.setSource(ScheduleSource.AGENT_NEGOTIATION);
        s.setStatus(ScheduleStatus.PROPOSED);
        s.setReviewRequired(true);

        return scheduleRepository.save(s);
    }

    private CoordinationRequestEntity loadRequest(Long coordinationId) {
        return coordinationRequestRepository
                .findByIdAndDelFalse(coordinationId)
                .orElseThrow(() -> new CoordinationFailedException(
                        "조율 요청을 찾을 수 없습니다: id=" + coordinationId));
    }

    // ---------- 검증 ----------

    private void validateRequest(CoordinationReq req) {
        if (req.userAIdx().equals(req.userBIdx())) {
            throw new CoordinationFailedException(
                    "같은 사용자끼리는 조율할 수 없습니다");
        }
        if (!req.searchFrom().isBefore(req.searchTo())) {
            throw new CoordinationFailedException(
                    "검색 시작 시각은 종료 시각보다 빨라야 합니다");
        }
        long searchRangeMinutes = ChronoUnit.MINUTES.between(
                req.searchFrom(), req.searchTo());
        if (searchRangeMinutes < req.durationMinutes()) {
            throw new CoordinationFailedException(
                    "검색 기간이 일정 길이보다 짧습니다");
        }
    }

    // ---------- 슬롯 후보 생성 ----------

    private List<SlotCandidate> generateAvailableSlots(
            Instant searchFrom,
            Instant searchTo,
            int durationMinutes,
            List<ScheduleEntity> userASchedules,
            List<ScheduleEntity> userBSchedules
    ) {
        List<SlotCandidate> result = new ArrayList<>();
        Duration duration = Duration.ofMinutes(durationMinutes);
        Duration step = Duration.ofMinutes(SLOT_GRANULARITY_MINUTES);

        Instant cursor = searchFrom;
        while (!cursor.plus(duration).isAfter(searchTo)) {
            Instant slotEnd = cursor.plus(duration);

            if (!hasConflict(cursor, slotEnd, userASchedules)
                    && !hasConflict(cursor, slotEnd, userBSchedules)) {
                double score = scoreSlot(cursor, slotEnd);
                result.add(new SlotCandidate(cursor, slotEnd, score));
            }

            cursor = cursor.plus(step);
        }

        return result;
    }

    private boolean hasConflict(
            Instant start,
            Instant end,
            List<ScheduleEntity> schedules
    ) {
        for (ScheduleEntity s : schedules) {
            if (s.getStatus() == ScheduleStatus.CANCELLED) {
                continue;
            }
            if (s.getStartTime().isBefore(end) && start.isBefore(s.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    // ---------- 점수 계산 (임시 휴리스틱) ----------

    /**
     * 슬롯 점수 계산 — 현재는 단순 휴리스틱.
     * 사용자 성향 데이터 처리 방식이 정해지면 이 메서드만 교체된다.
     */
    private double scoreSlot(Instant start, Instant end) {
        double score = 0.5;

        int dayOfWeek = start.atZone(ZoneOffset.UTC).getDayOfWeek().getValue();
        if (dayOfWeek >= 1 && dayOfWeek <= 5) {
            score += 0.3;
        } else {
            score -= 0.2;
        }

        int hour = start.atZone(ZoneOffset.UTC).getHour();
        if (hour >= 9 && hour < 18) {
            score += 0.2;
        }

        long daysFromNow = ChronoUnit.DAYS.between(Instant.now(), start);
        score += Math.max(0, 0.1 - daysFromNow * 0.01);

        return Math.max(0, Math.min(1.0, score));
    }
}