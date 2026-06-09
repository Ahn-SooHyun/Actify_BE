package co.kr.demo.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * 일정 수정 요청 (부분 수정).
 *
 * 모든 필드가 nullable이며, null인 필드는 "변경하지 않음"을 의미한다.
 * 서비스 계층에서 null이 아닌 필드만 골라 반영한다.
 */
public record ScheduleUpdateReq(

        @Size(max = 255)
        String title,

        @Size(max = 50)
        String category,

        Instant startTime,

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