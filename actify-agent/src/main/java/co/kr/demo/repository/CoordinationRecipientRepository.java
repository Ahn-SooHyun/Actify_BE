package co.kr.demo.repository;

import co.kr.demo.model.entity.CoordinationRecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 조율 참여자 데이터 접근 계층.
 *
 * 한 조율 요청(coordinationIdx) 아래에 묶인 참여자들을 조회하는 것이
 * 이 리포지토리의 핵심 역할이다.
 *
 * 검수 동기화(양쪽 ACCEPT/한쪽 DECLINE 처리)는 모두 coordinationIdx를
 * 출발점으로 시작되므로, 두 메서드 모두 그 컬럼을 기반으로 한다.
 */
public interface CoordinationRecipientRepository
        extends JpaRepository<CoordinationRecipientEntity, Long> {

    /**
     * 한 조율 요청에 묶인 모든 참여자 조회.
     * 회원 간 조율은 A·B 두 명이 반환됨.
     *
     * 거절·확정 처리 시 "함께 묶인 참여자들"의 상태를 종합 판단할 때 쓴다.
     */
    List<CoordinationRecipientEntity> findByCoordinationIdxAndDelFalse(
            Long coordinationIdx);

    /**
     * 특정 조율 요청에서 특정 사용자의 참여자 행 조회.
     * 한 사용자가 한 조율에 한 번만 참여하므로 단건 반환.
     *
     * "이 조율에서 사용자 A의 응답 상태를 ACCEPTED로 바꿔라" 같은
     * 처리에 쓴다.
     */
    Optional<CoordinationRecipientEntity>
    findByCoordinationIdxAndRecipientUsersIdxAndDelFalse(
            Long coordinationIdx, Long recipientUsersIdx);
}