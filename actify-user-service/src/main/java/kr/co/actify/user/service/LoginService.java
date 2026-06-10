package kr.co.actify.user.service;

import kr.co.actify.user.model.dto.login.LoginDTO;
import kr.co.actify.user.model.dto.login.LoginReq;
import kr.co.actify.user.model.dto.login.LogoutReq;

/**
 * 일반 로그인 및 로그아웃 처리를 위한 서비스 인터페이스입니다.
 * 아이디/비밀번호 기반의 인증과 토큰 발급 로직을 정의합니다.
 */
public interface LoginService {

    /**
     * 사용자의 아이디와 비밀번호를 검증하고, 유효한 경우 JWT 토큰(Access/Refresh)을 발급합니다.
     * @param loginReq 로그인 요청 정보 (아이디, 비밀번호)
     * @return 발급된 액세스 토큰과 리프레시 토큰이 담긴 DTO
     */
    LoginDTO login(LoginReq loginReq);

    String logout(LogoutReq logoutReq);
}