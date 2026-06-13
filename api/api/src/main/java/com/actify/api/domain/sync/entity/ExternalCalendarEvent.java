package com.actify.api.domain.sync.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_calendar_events")
public class ExternalCalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String calendarUserId;
    private String eventTitle;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // JPA 필수 기본 생성자 (Protected)
    protected ExternalCalendarEvent() {}

    // 수동 전체 필드 생성자 (서비스 레이어 에러 해결용)
    public ExternalCalendarEvent(Long id, String calendarUserId, String eventTitle, LocalDateTime startAt, LocalDateTime endAt) {
        this.id = id;
        this.calendarUserId = calendarUserId;
        this.eventTitle = eventTitle;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    // 수동 Getter 구현
    public Long getId() { return id; }
    public String getCalendarUserId() { return calendarUserId; }
    public String getEventTitle() { return eventTitle; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
}
