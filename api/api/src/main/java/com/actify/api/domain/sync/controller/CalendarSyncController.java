package com.actify.api.domain.sync.controller;

import com.actify.api.domain.sync.dto.ExternalEventRequest;
import com.actify.api.domain.sync.service.CalendarSyncService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sync")
public class CalendarSyncController {

    private final CalendarSyncService calendarSyncService;

    // 롬복을 지우고 수동 생성자 구현으로 변수 초기화(initialized) 에러 완벽 해결
    public CalendarSyncController(CalendarSyncService calendarSyncService) {
        this.calendarSyncService = calendarSyncService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveCalendarWebhook(@Valid @RequestBody ExternalEventRequest request) {
        calendarSyncService.syncIncomingEvent(request);
        return ResponseEntity.ok("외부 캘린더 데이터 동기화 백그라운드 태스크 가동");
    }
}
