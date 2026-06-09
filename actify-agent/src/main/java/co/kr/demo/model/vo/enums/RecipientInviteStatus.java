package co.kr.demo.model.vo.enums;

/**
 * 조율 참여자(Recipient)의 초대·응답 상태.
 * DB의 CK_CoordinationRecipients_Status 제약과 일치한다.
 *
 *   PENDING          : 응답 대기 중
 *   SENT             : 초대 발송됨 (이메일 발송 등)
 *   OPENED           : 참여자가 초대 링크/알림 열어봄
 *   ACCEPTED         : 수락
 *   DECLINED         : 거절
 *   COUNTER_PROPOSED : 역제안 (다른 시간 제안)
 *   EXPIRED          : 응답 기한 만료
 *   FAILED           : 발송 실패 등
 *
 * 우리 회원 간 조율 흐름에서 주로 쓰는 값:
 *   PENDING -> ACCEPTED   (참여자가 일정 수락)
 *   PENDING -> DECLINED   (참여자가 일정 거절)
 */
public enum RecipientInviteStatus {
    PENDING,
    SENT,
    OPENED,
    ACCEPTED,
    DECLINED,
    COUNTER_PROPOSED,
    EXPIRED,
    FAILED
}