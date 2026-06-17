package co.kr.demo.controller;

import co.kr.demo.model.dto.request.ScheduleCreateReq;
import co.kr.demo.model.dto.response.ScheduleRes;
import co.kr.demo.model.dto.request.ScheduleUpdateReq;
import co.kr.demo.model.vo.enums.ScheduleStatus;
import co.kr.demo.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * 일정 CRUD REST API.
 *
 *   POST   /api/schedules                         생성
 *   GET    /api/schedules/{id}                    단일 조회
 *   GET    /api/schedules?usersIdx=&from=&to=     사용자별 목록 (기간/상태 필터 선택)
 *   PATCH  /api/schedules/{id}                    부분 수정
 *   DELETE /api/schedules/{id}                    소프트 삭제
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ScheduleRes> create(
            @Valid @RequestBody ScheduleCreateReq req) {
        ScheduleRes body = ScheduleRes.from(service.create(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    public ScheduleRes get(@PathVariable Long id) {
        return ScheduleRes.from(service.get(id));
    }

    @GetMapping
    public List<ScheduleRes> list(
            @RequestParam Long usersIdx,
            @RequestParam(required = false) ScheduleStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        var schedules =
                (status != null) ? service.listByUserAndStatus(usersIdx, status)
                        : (from != null && to != null) ? service.listByUserInRange(usersIdx, from, to)
                          : service.listByUser(usersIdx);

        return schedules.stream().map(ScheduleRes::from).toList();
    }

    @PatchMapping("/{id}")
    public ScheduleRes update(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleUpdateReq req) {
        return ScheduleRes.from(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}