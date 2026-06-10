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

    boolean existsByIdAndDelAndStatusIn(String id, PublicDel del, List<UsersStatus> statuses);

    Optional<Users> findByUsersIdxAndStatusAndDel(Long usersIdx, UsersStatus status ,PublicDel del);

    Optional<Users> findByIdAndStatus(String id, UsersStatus status);
}