package com.actify.api.domain.coordination.controller;

import com.actify.api.domain.coordination.dto.GuestFeedbackRequest;
import com.actify.api.domain.coordination.dto.GuestProposalResponse;
import com.actify.api.domain.coordination.service.IntegrationCoordinationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guest/coordinations")
public class IntegrationCoordinationController {

    private final IntegrationCoordinationService coordinationService;

    // 롬복을 완전히 걷어내고 순수 자바 수동 생성자 주입 방식으로 초기화 에러 원천 해결
    public IntegrationCoordinationController(IntegrationCoordinationService coordinationService) {
        this.coordinationService = coordinationService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<GuestProposalResponse> viewProposalPage(@PathVariable("token") String token) {
        return ResponseEntity.ok(coordinationService.loadGuestSession(token));
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<String> submitAndFinalize(
            @PathVariable("id") Long id,
            @Valid @RequestBody GuestFeedbackRequest request) {
        coordinationService.collectAndFinalize(id, request);
        return ResponseEntity.ok("에이전트가 일정 취합 분석을 성공적으로 수행하였습니다.");
    }
}
