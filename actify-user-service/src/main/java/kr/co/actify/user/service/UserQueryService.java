package kr.co.actify.user.service;

import kr.co.actify.user.model.entity.Users;
import kr.co.actify.user.model.entity.UsersInformation;

/**
 * 사용자 정보의 단순 조회(Read-Only)를 전담하는 서비스 인터페이스입니다.
 * 데이터 변경 없이 엔티티를 찾거나 존재 여부를 확인하는 로직을 분리하여 재사용성을 높였습니다.
 * 주로 다른 서비스(Service) 구현체 내부에서 호출되어 사용됩니다.
 */
public interface UserQueryService {

    /**
     * 특정 아이디를 가진 활성 사용자가 존재하는지 확인합니다.
     * 회원가입 시 중복 아이디 체크 등에 사용됩니다.
     * * @param id 확인할 사용자 아이디
     * @return 존재하면 true, 아니면 false
     */
    boolean existsActiveId(String id);

    /**
     * 대기 상태(PENDING, 예: 이메일 인증 전)인 사용자를 조회합니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 대기 상태인 Users 엔티티
     * @throws IllegalArgumentException 사용자가 없거나 대기 상태가 아닐 경우
     */
    Users findWaitUser(Long userIdx);

    /**
     * 대기 상태인 사용자의 부가 정보(UsersInformation)를 조회합니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 대기 상태인 UsersInformation 엔티티
     */
    UsersInformation findWaitUserInfo(Long userIdx);


}