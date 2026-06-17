package co.kr.demo.service;

import co.kr.demo.model.dto.request.ScheduleCreateReq;
import co.kr.demo.model.dto.request.ScheduleUpdateReq;
import co.kr.demo.model.entity.ScheduleEntity;
import co.kr.demo.model.vo.enums.ScheduleStatus;

import java.time.Instant;
import java.util.List;

/**
 * 일정 서비스 인터페이스.
 *
 * 외부(컨트롤러)가 일정 도메인에 요청할 수 있는 동작 목록을 선언한다.
 * 실제 구현은 ScheduleServiceImpl에 있다.
 */
public interface ScheduleService {

    /** 일정 생성. */
    ScheduleEntity create(ScheduleCreateReq req);

    /** ID로 단일 일정 조회. 없으면 ScheduleNotFoundException. */
    ScheduleEntity get(Long id);

    /** 특정 사용자의 모든 일정 (시작시각 순). */
    List<ScheduleEntity> listByUser(Long usersIdx);

    /** 특정 사용자의 특정 기간 일정. */
    List<ScheduleEntity> listByUserInRange(Long usersIdx, Instant from, Instant to);

    /** 특정 사용자의 특정 상태 일정 (예: 검수 대기). */
    List<ScheduleEntity> listByUserAndStatus(Long usersIdx, ScheduleStatus status);

    /** 일정 부분 수정. null이 아닌 필드만 반영. */
    ScheduleEntity update(Long id, ScheduleUpdateReq req);

    /** 소프트 삭제 — Del=1로 표시. */
    void delete(Long id);
}