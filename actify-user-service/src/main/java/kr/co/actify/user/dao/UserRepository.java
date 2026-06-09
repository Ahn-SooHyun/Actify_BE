package kr.co.actify.user.dao;

import kr.co.actify.user.model.entity.Users;
import kr.co.actify.user.model.vo.PublicDel;
import kr.co.actify.user.model.vo.UsersStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Users 엔티티(사용자 계정 기본 정보)의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 아이디, 비밀번호, 권한 등 인증과 관련된 핵심 정보를 관리합니다.
 */
public interface UserRepository extends JpaRepository<Users, Long> {

    /**
     * 특정 아이디가 존재하는지 확인합니다.
     * 회원가입 시 아이디 중복 체크에 사용됩니다.
     *
     * @param id 확인할 사용자 아이디
     * @param del 삭제 상태
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByIdAndDelAndStatusIn(String id, PublicDel del, List<UsersStatus> statuses);

    /**
     * 사용자 식별자(PK)로 계정 정보를 조회합니다.
     *
     * @param usersIdx 사용자 식별자
     * @param del 삭제 상태
     * @return 조건에 맞는 Users 엔티티 (Optional)
     */
    Optional<Users> findByUsersIdxAndStatusAndDel(Long usersIdx, UsersStatus status ,PublicDel del);
}