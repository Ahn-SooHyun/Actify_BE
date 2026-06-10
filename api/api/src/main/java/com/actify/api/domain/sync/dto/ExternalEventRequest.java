package com.actify.api.domain.sync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ExternalEventRequest {

    @NotBlank private String calendarUserId;
    @NotBlank private String eventTitle;
    @NotNull private LocalDateTime startAt;
    @NotNull private LocalDateTime endAt;

    // 기본 생성자
    public ExternalEventRequest() {}

    // 수동 Getter / Setter 구현 목록 (롬복 의존성 완전 제거)
    public String getCalendarUserId() { return calendarUserId; }
    public void setCalendarUserId(String calendarUserId) { this.calendarUserId = calendarUserId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
}
