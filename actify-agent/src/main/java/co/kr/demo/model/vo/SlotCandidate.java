package co.kr.demo.model.vo;

import java.time.Instant;

/**
 * 조율 과정에서 생성되는 슬롯 후보.
 *
 * 시작·종료 시각과 점수를 담는 값 객체. DB에 저장되지 않고
 * 조율 로직 안에서만 메모리에 잠시 존재한다.
 *
 * 점수(score)는 클수록 좋은 슬롯을 의미한다.
 * 성향 데이터 처리 방식이 확정되면 점수 계산 로직만 교체된다.
 */
public record SlotCandidate(
        Instant start,
        Instant end,
        double score
) {
}