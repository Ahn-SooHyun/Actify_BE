package kr.co.actify.user.dao;

import kr.co.actify.user.model.entity.UsersInformation;
import kr.co.actify.user.model.vo.PublicDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UsersInformation 엔티티(사용자 상세 정보)의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 이름, 이메일, 전화번호 등 개인정보를 관리합니다.
 */
public interface UserInformationRepository extends JpaRepository<UsersInformation, Long> {

    /**
     * 특정 사용자의 상세 정보를 조회합니다.
     *
     * @param usersIdx 사용자 식별자 (PK)
     * @param del 삭제 상태
     * @return 조건에 맞는 UsersInformation 엔티티 (Optional)
     */
    Optional<UsersInformation> findByUsersIdxAndDel(Long usersIdx, PublicDel del);

}