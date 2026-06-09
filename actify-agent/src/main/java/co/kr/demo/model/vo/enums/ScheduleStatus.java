package co.kr.demo.model.vo.enums;

/**
 * 일정 상태. DB의 CK_Schedules_Status 제약과 정확히 일치해야 한다.
 *   DRAFT          : 초안 (AI 생성 직후 등)
 *   PROPOSED       : 제안됨 (조율/협상 후보)
 *   PENDING_REVIEW : 사용자 검수 대기
 *   CONFIRMED      : 확정
 *   CANCELLED      : 취소
 */
public enum ScheduleStatus {
    DRAFT,
    PROPOSED,
    PENDING_REVIEW,
    CONFIRMED,
    CANCELLED
}