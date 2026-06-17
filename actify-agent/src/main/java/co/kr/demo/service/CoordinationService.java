package co.kr.demo.service;

import co.kr.demo.model.dto.request.CoordinationReq;
import co.kr.demo.model.dto.response.CoordinationRes;

/**
 * 회원 간 일정 조율 서비스 인터페이스.
 *
 * 단일 에이전트가 DB에서 두 사용자의 일정과 성향을 모두 조회한 뒤
 * 최적의 빈 슬롯을 찾아 PROPOSED 상태의 일정을 양측 캘린더에 생성한다.
 *
 * 검수 동기화 정책:
 *   양측 모두 ACCEPT → 양측 일정 모두 CONFIRMED
 *   한쪽이라도 DECLINE → 양측 일정 모두 CANCELLED
 *
 * 실제 구현은 CoordinationServiceImpl에 있다.
 */
public interface CoordinationService {

    /**
     * 두 회원 간 일정을 조율한다.
     *
     * 동작:
     *  1. 두 사용자의 검색 기간 내 일정을 DB에서 조회
     *  2. 양쪽 모두 비어있는 슬롯 후보를 생성
     *  3. 각 후보에 점수를 매기고 최고점을 선택
     *  4. Coordination_Requests에 조율 요청 마스터 저장
     *  5. Coordination_Recipients에 참여자 두 명 저장
     *  6. 선택된 슬롯으로 두 사용자의 일정을 각각 생성(PROPOSED)
     *
     * @param req 두 사용자 id, 일정 길이, 검색 기간, 제목 등
     * @return 선택된 슬롯과 생성된 두 일정의 id, 조율 요청 id를 담은 응답
     * @throws co.kr.demo.exception.CoordinationFailedException
     *         양쪽 모두 가능한 슬롯이 없거나 검증 실패 시
     */
    CoordinationRes coordinate(CoordinationReq req);

    /**
     * 참여자가 조율 결과를 수락한다.
     *
     * 동작:
     *  1. 해당 참여자의 invite_status를 ACCEPTED로 변경
     *  2. 모든 참여자가 ACCEPTED인지 확인
     *  3. 모두 ACCEPTED면 조율 요청 status를 CONFIRMED로,
     *     양측 일정의 status도 CONFIRMED로 전환
     *  4. 일부만 ACCEPTED면 조율은 NEGOTIATING 유지
     *
     * @param coordinationId 조율 요청 id
     * @param usersIdx       수락하는 참여자의 사용자 id
     * @throws co.kr.demo.exception.CoordinationFailedException
     *         조율 요청이 없거나 해당 사용자가 참여자가 아닐 때
     */
    void accept(Long coordinationId, Long usersIdx);

    /**
     * 참여자가 조율 결과를 거절한다.
     *
     * 동작:
     *  1. 해당 참여자의 invite_status를 DECLINED로 변경
     *  2. 조율 요청 status를 CANCELLED로 전환
     *  3. 양측 일정의 status를 모두 CANCELLED로 전환
     *
     * @param coordinationId 조율 요청 id
     * @param usersIdx       거절하는 참여자의 사용자 id
     * @throws co.kr.demo.exception.CoordinationFailedException
     *         조율 요청이 없거나 해당 사용자가 참여자가 아닐 때
     */
    void decline(Long coordinationId, Long usersIdx);
}