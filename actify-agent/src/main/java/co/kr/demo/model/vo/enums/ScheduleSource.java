package co.kr.demo.model.vo.enums;

/**
 * 일정 생성 출처. DB의 CK_Schedules_Source 제약과 일치한다.
 *   MANUAL            : 사용자 수동 입력
 *   AI                : 단일 Agent가 사용자 한 명을 위해 생성
 *   COORDINATION      : 비회원 대상 이메일 조율 결과
 *   AGENT_NEGOTIATION : 회원 간 조율 결과 (단일 Agent가 DB의 여러 사용자
 *                        일정·성향을 종합해 산출한 슬롯)
 *   IMPORT            : 외부 데이터 가져오기
 *   EXTERNAL_SYNC     : 외부 캘린더 동기화
 */
public enum ScheduleSource {
    MANUAL,
    AI,
    COORDINATION,
    AGENT_NEGOTIATION,
    IMPORT,
    EXTERNAL_SYNC
}