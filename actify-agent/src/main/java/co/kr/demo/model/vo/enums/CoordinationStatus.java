package co.kr.demo.model.vo.enums;

/**
 * 조율 요청의 상태. DB의 CK_CoordinationRequests_Status 제약과 일치한다.
 *
 *   DRAFT       : 초안 (요청 생성 직전)
 *   SENT        : 요청 발송됨 (참여자에게 알림이 간 상태)
 *   RESPONDED   : 일부 응답 들어옴
 *   NEGOTIATING : 조율 진행 중 (참여자 검수 대기)
 *   CONFIRMED   : 양측 합의 완료, 일정 확정
 *   CANCELLED   : 취소 (한쪽이라도 거절 시)
 *   EXPIRED     : 만료 (응답 기한 초과)
 *   FAILED      : 실패 (가능 슬롯 없음 등)
 *
 * 우리 회원 간 조율 흐름에서 주로 쓰는 값:
 *   NEGOTIATING -> CONFIRMED  (양측 모두 ACCEPT)
 *   NEGOTIATING -> CANCELLED  (한쪽이라도 DECLINE)
 */
public enum CoordinationStatus {
    DRAFT,
    SENT,
    RESPONDED,
    NEGOTIATING,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    FAILED
}