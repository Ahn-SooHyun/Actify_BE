package co.kr.demo.model.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;

/**
 * 회원 간 일정 조율 요청.
 *
 * 두 사용자의 id, 일정 길이, 검색 기간, 일정 제목을 받아
 * 조율 엔진이 최적 슬롯을 찾는다.
 *
 * 검증 규칙:
 *  - 두 사용자 id는 필수이고 서로 달라야 한다 (서비스에서 추가 검증)
 *  - 일정 길이는 양수, 8시간 이내
 *  - 검색 기간 from < to (서비스에서 검증)
 *  - 제목은 필수
 */
public record CoordinationReq(

        @NotNull(message = "사용자 A의 식별자는 필수입니다")
        Long userAIdx,

        @NotNull(message = "사용자 B의 식별자는 필수입니다")
        Long userBIdx,

        @NotNull(message = "일정 길이는 필수입니다")
        @Min(value = 15, message = "일정은 최소 15분 이상이어야 합니다")
        @Max(value = 480, message = "일정은 최대 8시간을 넘을 수 없습니다")
        Integer durationMinutes,

        @NotNull(message = "검색 시작 시각은 필수입니다")
        Instant searchFrom,

        @NotNull(message = "검색 종료 시각은 필수입니다")
        Instant searchTo,

        @NotBlank(message = "일정 제목은 필수입니다")
        @Size(max = 255)
        String title,

        @Size(max = 50)
        String category,

        @Size(max = 512)
        String address,

        String description
) {
}