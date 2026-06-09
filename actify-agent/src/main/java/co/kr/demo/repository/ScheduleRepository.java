package co.kr.demo.repository;

import co.kr.demo.model.entity.ScheduleEntity;
import co.kr.demo.model.vo.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 일정 데이터 접근 계층.
 *
 * 이 앱은 소프트 삭제(Del=1)를 쓰므로, 모든 조회는 del=false 조건을
 * 포함해야 한다. 그렇지 않으면 삭제된 일정이 조회 결과에 섞인다.
 */
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    /** 삭제되지 않은 단일 일정 조회. */
    Optional<ScheduleEntity> findByIdAndDelFalse(Long id);

    /** 특정 사용자의 삭제되지 않은 모든 일정을 시작시각 순으로. */
    List<ScheduleEntity> findByUsersIdxAndDelFalseOrderByStartTime(Long usersIdx);

    /** 특정 사용자의 특정 기간 일정 (캘린더 월/주 뷰 등에 사용). */
    List<ScheduleEntity> findByUsersIdxAndDelFalseAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
            Long usersIdx, Instant from, Instant to);

    /** 특정 사용자의 특정 상태 일정 (예: 검수 대기 목록). */
    List<ScheduleEntity> findByUsersIdxAndStatusAndDelFalseOrderByStartTime(
            Long usersIdx, ScheduleStatus status);

    /**
     * 시간 충돌 검사.
     * 겹침 조건: 기존.start < 신규.end AND 신규.start < 기존.end
     * 삭제/취소된 일정은 제외. excludeId로 자기 자신을 제외(수정 시 사용).
     */
    @Query("""
           SELECT COUNT(s) > 0 FROM Schedule s
           WHERE s.usersIdx = :usersIdx
             AND s.del = false
             AND s.status <> co.kr.demo.entity.ScheduleStatus.CANCELLED
             AND s.startTime < :end
             AND :start < s.endTime
             AND (:excludeId IS NULL OR s.id <> :excludeId)
           """)
    boolean existsConflict(
            @Param("usersIdx") Long usersIdx,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("excludeId") Long excludeId);
}