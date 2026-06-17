package co.kr.demo.controller;

import co.kr.demo.model.dto.request.CoordinationReq;
import co.kr.demo.model.dto.response.CoordinationRes;
import co.kr.demo.service.CoordinationService;
import co.kr.demo.util.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 간 일정 조율 REST API.
 *
 * 엔드포인트:
 *   POST /api/actify/coordination/request          조율 요청 (양측 PROPOSED 일정 생성)
 *   POST /api/actify/coordination/accept/{id}      참여자가 조율 결과 수락
 *   POST /api/actify/coordination/decline/{id}     참여자가 조율 결과 거절
 *
 * 검수 흐름:
 *   1. /request 호출 시 양측에 PROPOSED 일정 두 행 생성, 응답에 coordinationId 포함
 *   2. 사용자 A·B가 각자 /accept 또는 /decline을 coordinationId와 usersIdx로 호출
 *   3. 양측 모두 ACCEPT 시 양측 일정 CONFIRMED
 *      한쪽이라도 DECLINE 시 양측 일정 CANCELLED
 */
@RestController
@RequestMapping("/api/actify/coordination")
public class CoordinationController {

    private final CoordinationService service;

    public CoordinationController(CoordinationService service) {
        this.service = service;
    }

    @PostMapping("/request")
    public ResponseEntity<BaseResponse<CoordinationRes>> request(
            @Valid @RequestBody CoordinationReq req
    ) {
        CoordinationRes data = service.coordinate(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.ok("일정 조율이 완료되었습니다", data));
    }

    @PostMapping("/accept/{id}")
    public BaseResponse<Void> accept(
            @PathVariable("id") Long coordinationId,
            @RequestParam Long usersIdx
    ) {
        service.accept(coordinationId, usersIdx);
        return BaseResponse.ok("수락이 처리되었습니다", null);
    }

    @PostMapping("/decline/{id}")
    public BaseResponse<Void> decline(
            @PathVariable("id") Long coordinationId,
            @RequestParam Long usersIdx
    ) {
        service.decline(coordinationId, usersIdx);
        return BaseResponse.ok("거절이 처리되었습니다. 양측 일정이 취소됩니다", null);
    }
}