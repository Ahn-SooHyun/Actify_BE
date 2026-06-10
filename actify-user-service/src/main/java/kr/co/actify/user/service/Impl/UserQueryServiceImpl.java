package kr.co.actify.user.service.Impl;

import kr.co.actify.user.dao.UserInformationRepository;
import kr.co.actify.user.dao.UserRepository;
import kr.co.actify.user.model.entity.Users;
import kr.co.actify.user.model.entity.UsersInformation;
import kr.co.actify.user.model.vo.PublicDel;
import kr.co.actify.user.model.vo.UsersStatus;
import kr.co.actify.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserQueryService 인터페이스의 구현체입니다.
 * 사용자 정보 조회를 전담하며, 반복적인 조회 로직을 중앙화하여 관리합니다.
 * 주로 다른 서비스 레이어에서 호출하여 사용합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 모든 메서드에 대해 기본적으로 읽기 전용 트랜잭션 적용
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;

    /**
     * 활성 상태인 특정 아이디가 존재하는지 확인합니다.
     * 회원가입 시 아이디 중복 체크용입니다.
     */
    @Override
    public boolean existsActiveId(String id) {
        List<UsersStatus> validStatuses = List.of(UsersStatus.PENDING_EMAIL, UsersStatus.ACTIVE);

        return userRepository.existsByIdAndDelAndStatusIn(
                id,
                PublicDel.ACTIVE,
                validStatuses
        );
    }

    /**
     * 대기 상태(PENDING)의 사용자를 식별자로 조회합니다.
     * 가입 승인 대기 중인 사용자를 찾을 때 사용합니다.
     */
    @Override
    public Users findWaitUser(Long userIdx) {
        return userRepository.findByUsersIdxAndStatusAndDel(userIdx, UsersStatus.PENDING_EMAIL, PublicDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 대기 상태가 아닌 사용자입니다."));
    }

    /**
     * 대기 상태인 사용자의 상세 정보(UsersInformation)를 조회합니다.
     */
    @Override
    public UsersInformation findWaitUserInfo(Long userIdx) {
        return userInformationRepository.findByUsersIdxAndDel(userIdx, PublicDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 대기 상태가 아닌 사용자입니다."));
    }

    /**
     * 활성 상태(ACTIVE)의 사용자를 아이디(String)로 조회합니다.
     * 로그인 등에 사용됩니다.
     */
    @Override
    public Users findActiveUserById(String id) {
        Users user = userRepository.findByIdAndStatus(id, UsersStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        user.checkAccountStatus();
        return user;
    }

}