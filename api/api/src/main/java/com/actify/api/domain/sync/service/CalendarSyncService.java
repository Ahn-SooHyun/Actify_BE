package com.actify.api.domain.sync.service;

import com.actify.api.domain.sync.dto.ExternalEventRequest;
import com.actify.api.domain.sync.entity.ExternalCalendarEvent;
import com.actify.api.domain.sync.repository.CalendarSyncRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarSyncService {

    // 롬복 없이 순수 자바 로거 생성
    private static final Logger log = LoggerFactory.getLogger(CalendarSyncService.class);

    private final CalendarSyncRepository calendarSyncRepository;

    // 롬복 없이 수동 생성자 주입 (DI) 구현으로 'might not have been initialized' 에러 원천 차단
    public CalendarSyncService(CalendarSyncRepository calendarSyncRepository) {
        this.calendarSyncRepository = calendarSyncRepository;
    }

    @Async("calendarSyncExecutor")
    @Transactional
    public void syncIncomingEvent(ExternalEventRequest request) {
        log.info("[Sync Agent] 외부 캘린더 실시간 스케줄 감지 - 사용자: {}", request.getCalendarUserId());

        // 순수 자바 표준 생성자로 매핑
        ExternalCalendarEvent event = new ExternalCalendarEvent(
                null,
                request.getCalendarUserId(),
                request.getEventTitle(),
                request.getStartAt(),
                request.getEndAt()
        );

        calendarSyncRepository.save(event);
        log.info("[Sync Agent] 데이터 적재 및 동기화 완료: '{}'", request.getEventTitle());
    }
}
