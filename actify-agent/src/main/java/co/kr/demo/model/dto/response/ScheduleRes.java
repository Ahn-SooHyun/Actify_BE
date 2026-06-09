package co.kr.demo.model.dto.response;

import co.kr.demo.model.entity.ScheduleEntity;
import co.kr.demo.model.vo.enums.ScheduleSource;
import co.kr.demo.model.vo.enums.ScheduleStatus;

import java.time.Instant;

/**
 * 일정 응답.
 *
 * 엔티티를 직접 노출하지 않고 이 record로 변환해 반환한다.
 */
public record ScheduleRes(
        Long id,
        Long usersIdx,
        Long assistantIdx,
        String title,
        String category,
        Instant startTime,
        Instant endTime,
        String timezone,
        boolean allDay,
        String address,
        String addressDetail,
        String onlineUrl,
        String description,
        ScheduleSource source,
        ScheduleStatus status,
        boolean reviewRequired,
        int priority,
        String recurrenceRule,
        Instant confirmedAt,
        Instant createdAt,
        Instant updatedAt
) {
    /** 엔티티 -> 응답 DTO 변환. */
    public static ScheduleRes from(ScheduleEntity s) {
        return new ScheduleRes(
                s.getId(),
                s.getUsersIdx(),
                s.getAssistantIdx(),
                s.getTitle(),
                s.getCategory(),
                s.getStartTime(),
                s.getEndTime(),
                s.getTimezone(),
                s.isAllDay(),
                s.getAddress(),
                s.getAddressDetail(),
                s.getOnlineUrl(),
                s.getDescription(),
                s.getSource(),
                s.getStatus(),
                s.isReviewRequired(),
                s.getPriority(),
                s.getRecurrenceRule(),
                s.getConfirmedAt(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}