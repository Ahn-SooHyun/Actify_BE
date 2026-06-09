package co.kr.demo.model.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;

/**
 * 일정 생성 요청.
 *
 * 필수값(usersIdx, title, 시작/종료)에 검증을 걸고, 나머지는 선택이다.
 * 상태(status)나 출처(source)는 서버가 결정하므로 요청에 받지 않는다.
 */
public record ScheduleCreateReq(

        @NotNull(message = "사용자 식별자는 필수입니다")
        Long usersIdx,

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 255)
        String title,

        @Size(max = 50)
        String category,

        @NotNull(message = "시작 시각은 필수입니다")
        Instant startTime,

        @NotNull(message = "종료 시각은 필수입니다")
        Instant endTime,

        @Size(max = 64)
        String timezone,

        Boolean allDay,

        @Size(max = 512)
        String address,

        @Size(max = 512)
        String addressDetail,

        @Size(max = 1024)
        String onlineUrl,

        String description,

        @Min(1) @Max(5)
        Integer priority,

        @Size(max = 512)
        String recurrenceRule
) {
}