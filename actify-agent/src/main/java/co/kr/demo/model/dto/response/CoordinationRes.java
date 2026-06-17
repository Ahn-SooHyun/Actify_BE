package co.kr.demo.model.dto.response;

import co.kr.demo.model.entity.CoordinationRequestEntity;
import co.kr.demo.model.entity.ScheduleEntity;

import java.time.Instant;

/**
 * 회원 간 일정 조율 결과.
 *
 * 조율 엔진이 선택한 최적 슬롯 정보와, 그 슬롯으로 생성된
 * 두 사용자의 일정 id, 그리고 향후 검수에 필요한 조율 요청 id를 담아 반환한다.
 *
 * 두 일정은 모두 PROPOSED 상태로 저장되어, 양측 사용자가
 * coordinationId로 accept/decline API를 호출해 검수한다.
 */
public record CoordinationRes(
        Long coordinationId,
        Instant chosenStart,
        Instant chosenEnd,
        double chosenScore,
        Long userAScheduleId,
        Long userBScheduleId,
        String title,
        String message
) {
    /**
     * 조율 요청과 결과로 만들어진 두 일정으로부터 응답 생성.
     */
    public static CoordinationRes from(
            CoordinationRequestEntity request,
            ScheduleEntity userAEntity,
            ScheduleEntity userBEntity,
            double chosenScore
    ) {
        return new CoordinationRes(
                request.getId(),
                userAEntity.getStartTime(),
                userAEntity.getEndTime(),
                chosenScore,
                userAEntity.getId(),
                userBEntity.getId(),
                userAEntity.getTitle(),
                "조율이 완료되었습니다. 양측 검수를 기다리는 중입니다"
        );
    }
}