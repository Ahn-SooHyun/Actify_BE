package co.kr.demo.service.impl;

import co.kr.demo.exception.ScheduleConflictException;
import co.kr.demo.exception.ScheduleNotFoundException;
import co.kr.demo.model.dto.request.ScheduleCreateReq;
import co.kr.demo.model.dto.request.ScheduleUpdateReq;
import co.kr.demo.model.entity.ScheduleEntity;
import co.kr.demo.model.vo.enums.ScheduleStatus;
import co.kr.demo.repository.ScheduleRepository;
import co.kr.demo.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.kr.demo.model.vo.enums.ScheduleSource;

import java.time.Instant;
import java.util.List;

/**
 * 일정 서비스의 구현체.
 *
 * 인터페이스(ScheduleService)에 선언된 메서드들의 실제 동작을 담는다.
 * 비즈니스 검증과 규칙(시간 유효성, 충돌 검사, 소프트 삭제)이 모두 여기 모인다.
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository repository;

    public ScheduleServiceImpl(ScheduleRepository repository) {
        this.repository = repository;
    }

    // ---------- CREATE ----------

    @Override
    @Transactional
    public ScheduleEntity create(ScheduleCreateReq req) {
        validateTimeRange(req.startTime(), req.endTime());
        ensureNoConflict(req.usersIdx(), req.startTime(), req.endTime(), null);

        ScheduleEntity s = new ScheduleEntity(
                req.usersIdx(), req.title(), req.startTime(), req.endTime());

        // 선택 필드 — null이면 엔티티 기본값 유지
        if (req.category() != null) s.setCategory(req.category());
        if (req.timezone() != null) s.setTimezone(req.timezone());
        if (req.allDay() != null) s.setAllDay(req.allDay());
        if (req.address() != null) s.setAddress(req.address());
        if (req.addressDetail() != null) s.setAddressDetail(req.addressDetail());
        if (req.onlineUrl() != null) s.setOnlineUrl(req.onlineUrl());
        if (req.description() != null) s.setDescription(req.description());
        if (req.priority() != null) s.setPriority(req.priority());
        if (req.recurrenceRule() != null) s.setRecurrenceRule(req.recurrenceRule());

        return repository.save(s);
    }

    // ---------- READ ----------

    @Override
    @Transactional(readOnly = true)
    public ScheduleEntity get(Long id) {
        return repository.findByIdAndDelFalse(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEntity> listByUser(Long usersIdx) {
        return repository.findByUsersIdxAndDelFalseOrderByStartTime(usersIdx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEntity> listByUserInRange(Long usersIdx, Instant from, Instant to) {
        validateTimeRange(from, to);
        return repository
                .findByUsersIdxAndDelFalseAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
                        usersIdx, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEntity> listByUserAndStatus(Long usersIdx, ScheduleStatus status) {
        return repository.findByUsersIdxAndStatusAndDelFalseOrderByStartTime(
                usersIdx, status);
    }

    // ---------- UPDATE ----------

    @Override
    @Transactional
    public ScheduleEntity update(Long id, ScheduleUpdateReq req) {
        ScheduleEntity s = get(id);

        if (req.title() != null) s.setTitle(req.title());
        if (req.category() != null) s.setCategory(req.category());
        if (req.timezone() != null) s.setTimezone(req.timezone());
        if (req.allDay() != null) s.setAllDay(req.allDay());
        if (req.address() != null) s.setAddress(req.address());
        if (req.addressDetail() != null) s.setAddressDetail(req.addressDetail());
        if (req.onlineUrl() != null) s.setOnlineUrl(req.onlineUrl());
        if (req.description() != null) s.setDescription(req.description());
        if (req.priority() != null) s.setPriority(req.priority());
        if (req.recurrenceRule() != null) s.setRecurrenceRule(req.recurrenceRule());

        // 시간 변경 시 검증 + 충돌 재확인 (자기 자신 제외)
        Instant newStart = req.startTime() != null ? req.startTime() : s.getStartTime();
        Instant newEnd = req.endTime() != null ? req.endTime() : s.getEndTime();
        if (req.startTime() != null || req.endTime() != null) {
            // 조율 결과 일정은 시간 변경 금지 — 짝 일정과 어긋날 수 있음
            // 시간을 바꾸려면 일정을 거절하고 조율을 다시 요청해야 한다.
            if (s.getSource() == ScheduleSource.AGENT_NEGOTIATION) {
                throw new ScheduleConflictException(
                        "조율로 생성된 일정의 시간은 직접 수정할 수 없습니다. " +
                                "거절 후 조율을 다시 요청해주세요");
            }
            validateTimeRange(newStart, newEnd);
            ensureNoConflict(s.getUsersIdx(), newStart, newEnd, id);
            s.setStartTime(newStart);
            s.setEndTime(newEnd);
        }

        // dirty checking으로 자동 반영됨
        return s;
    }

    // ---------- DELETE (소프트 삭제) ----------

    @Override
    @Transactional
    public void delete(Long id) {
        ScheduleEntity s = get(id);
        s.softDelete();
    }

    // ---------- 검증 헬퍼 (인터페이스에는 노출되지 않음) ----------

    private void validateTimeRange(Instant start, Instant end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new ScheduleConflictException(
                    "시작 시각은 종료 시각보다 빨라야 합니다");
        }
    }

    private void ensureNoConflict(Long usersIdx, Instant start, Instant end,
                                  Long excludeId) {
        if (repository.existsConflict(usersIdx, start, end, excludeId)) {
            throw new ScheduleConflictException(
                    "해당 시간대에 이미 다른 일정이 있습니다");
        }
    }
}