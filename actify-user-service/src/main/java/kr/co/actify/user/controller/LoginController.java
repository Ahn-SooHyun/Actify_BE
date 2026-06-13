package kr.co.actify.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.actify.user.model.dto.login.LoginDTO;
import kr.co.actify.user.model.dto.login.LoginReq;
import kr.co.actify.user.model.dto.login.LogoutReq;
import kr.co.actify.user.service.LoginService;
import kr.co.actify.user.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인 및 로그아웃과 관련된 요청을 처리하는 컨트롤러입니다.
 * 사용자의 인증을 수행하고, JWT 토큰(Access/Refresh)을 쿠키에 설정하거나 삭제합니다.
 */
@Validated // 데이터 유효성 검증(@Valid 등) 기능을 활성화합니다.
@RestController // JSON 형태의 응답을 반환하는 REST 컨트롤러임을 명시합니다.
@RequiredArgsConstructor // final로 선언된 필드에 대해 생성자를 자동으로 생성합니다.
@RequestMapping("/auth") // 이 컨트롤러의 모든 API 경로는 "/auth"로 시작합니다.
public class LoginController {
    // 로그인 비즈니스 로직을 처리하는 서비스 주입
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginDTO>> login(@RequestBody @Valid LoginReq loginReq) {
        LoginDTO loginDTO = loginService.login(loginReq);

        return ResponseEntity.ok(
                new BaseResponse<>("SUCCESS", loginDTO)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(@RequestBody @Valid LogoutReq logoutReq) {
        return ResponseEntity.ok(
                new BaseResponse<>("SUCCESS", loginService.logout(logoutReq))
        );
    }
}