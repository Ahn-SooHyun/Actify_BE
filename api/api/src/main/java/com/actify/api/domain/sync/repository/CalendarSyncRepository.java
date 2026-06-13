package com.actify.api.domain.sync.repository;

import com.actify.api.domain.sync.entity.ExternalCalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CalendarSyncRepository extends JpaRepository<ExternalCalendarEvent, Long> {
    List<ExternalCalendarEvent> findByCalendarUserId(String calendarUserId);
}
