package co.kr.demo.repository;

import co.kr.demo.model.entity.CoordinationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 조율 요청 마스터 데이터 접근 계층.
 *
 * 조율 요청 한 건의 생애주기(NEGOTIATING → CONFIRMED 또는 CANCELLED)를
 * 추적하기 위한 기본 조회·저장 기능을 제공한다.
 *
 * JpaRepository를 상속하므로 save, findById, deleteById 등은
 * 자동으로 제공된다. 추가 메서드만 여기 선언한다.
 */
public interface CoordinationRequestRepository
        extends JpaRepository<CoordinationRequestEntity, Long> {

    /** 삭제되지 않은 조율 요청 단건 조회. */
    Optional<CoordinationRequestEntity> findByIdAndDelFalse(Long id);
}