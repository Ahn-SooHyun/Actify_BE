package co.kr.demo.model.vo.enums;

/**
 * 조율 처리 방식. DB의 CK_CoordinationRequests_Mode 제약과 일치한다.
 *
 *   SINGLE_AGENT_EMAIL : 비회원 대상 이메일 조율
 *                        (단일 에이전트가 이메일로 비회원의 가용 정보를 수집)
 *   MULTI_AGENT        : 앱 회원 간 조율
 *                        (단일 에이전트가 DB의 회원 일정·성향을 모두 보고 조율)
 *                        ※ 이름은 옛 다중 에이전트 협상 설계의 흔적이지만,
 *                        지금은 단일 에이전트가 여러 회원을 처리하는 방식을 의미한다.
 *   MIXED_AGENT_EMAIL  : 회원+비회원 혼합 조율
 *   MANUAL             : 수동 조율
 */
public enum CoordinationMode {
    SINGLE_AGENT_EMAIL,
    MULTI_AGENT,
    MIXED_AGENT_EMAIL,
    MANUAL
}