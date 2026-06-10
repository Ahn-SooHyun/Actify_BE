package kr.co.actify.user.service.Impl;

import kr.co.actify.user.global.security.jwt.JWTUtil;
import kr.co.actify.user.global.security.jwt.RefreshTokenUtil;
import kr.co.actify.user.model.dto.login.LoginDTO;
import kr.co.actify.user.model.dto.login.LoginReq;
import kr.co.actify.user.model.dto.login.LogoutReq;
import kr.co.actify.user.model.entity.Users;
import kr.co.actify.user.service.LoginService;
import kr.co.actify.user.service.UserQueryService;
import kr.co.actify.user.util.BCryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * LoginService 인터페이스의 구현체입니다.
 * 로그인 및 로그아웃과 관련된 비즈니스 로직을 처리합니다.
 *
 * Access Token은 JWT로 발급하고,
 * Refresh Token은 SecureRandom 기반 랜덤 문자열로 발급합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginServiceImpl implements LoginService {

    // 비밀번호 암호화 및 검증 유틸리티
    private final BCryptUtil bCryptUtil;

    // Access Token JWT 생성 및 검증 유틸리티
    private final JWTUtil jwtUtil;

    // Redis 작업을 위한 템플릿
    private final RedisTemplate<String, Object> redisTemplate;

    // 사용자 정보 조회를 위한 공통 서비스
    private final UserQueryService userQueryService;

    // SecureRandom 기반 Refresh Token 생성 유틸리티
    private final RefreshTokenUtil refreshTokenUtil;

    // application.yml에서 설정된 로그인 실패 허용 횟수
    @Value("${custom.security.login.max-attempts}")
    private int maxAttempts;

    // 로그인 실패로 인한 계정 잠금 시간
    @Value("${custom.security.login.lock-minutes}")
    private int lockMinutes;

    // Redis에 저장될 Refresh Token 키의 접두사
    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix;

    // Redis에 저장될 Blacklist 키의 접두사
    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix;

    /**
     * 로그인을 수행합니다.
     * 아이디와 비밀번호를 검증하고,
     * 성공 시 Access Token JWT와 Refresh Token을 발급합니다.
     *
     * Redis 저장 구조:
     * - RT:{userIdx}            -> refreshToken
     * - RT:token:{refreshToken} -> userIdx
     *
     * @param loginReq 로그인 요청 정보
     * @return 발급된 토큰 정보가 담긴 DTO
     */
    @Override
    @Transactional
    public LoginDTO login(LoginReq loginReq) {
        // 아이디로 활성 사용자 조회
        Users user = userQueryService.findActiveUserById(loginReq.getId());

        // 계정 잠금 여부 확인
        if (user.isLocked()) {
            throw new IllegalStateException("계정이 잠금 상태입니다. 해제 일시: " + user.getLockedUntil());
        }

        // 비밀번호 검증
        if (!bCryptUtil.matches(loginReq.getPw(), user.getPw())) {
            user.handleLoginFailure(maxAttempts, lockMinutes);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 권한 정보 확인
        if (user.getRole() == null) {
            throw new IllegalStateException("사용자 권한 정보가 존재하지 않습니다.");
        }

        // 로그인 성공 처리
        user.completeLogin();

        long refreshTokenTtlSeconds = refreshTokenUtil.getRefreshTokenExpirationSeconds();

        /*
         * 기존 Refresh Token 정리
         *
         * 현재 구조는 사용자 1명당 Refresh Token 1개만 유지하는 방식이다.
         * 새 로그인이 발생하면 기존 Refresh Token을 제거하고,
         * 재사용 방지를 위해 blacklist에 등록한다.
         */
        String rtKey = createRefreshTokenKey(user.getUsersIdx());

        Object oldRefreshTokenValue = redisTemplate.opsForValue().get(rtKey);

        if (oldRefreshTokenValue != null) {
            String oldRefreshToken = String.valueOf(oldRefreshTokenValue);

            Long oldTokenRemainingTtlSeconds = redisTemplate.getExpire(rtKey, TimeUnit.SECONDS);
            long blacklistTtlSeconds = resolvePositiveTtl(
                    oldTokenRemainingTtlSeconds,
                    refreshTokenTtlSeconds
            );

            redisTemplate.delete(rtKey);
            redisTemplate.delete(createRefreshTokenReverseKey(oldRefreshToken));

            redisTemplate.opsForValue().set(
                    createBlacklistKey(oldRefreshToken),
                    "logout",
                    blacklistTtlSeconds,
                    TimeUnit.SECONDS
            );
        }

        /*
         * Access Token 생성
         *
         * JWT subject에는 로그인 ID가 아니라 사용자 PK를 넣는다.
         */
        String accessToken = jwtUtil.createAccessToken(
                user.getUsersIdx(),
                user.getRole().name()
        );

        /*
         * Refresh Token 생성
         *
         * JWT가 아니라 SecureRandom 기반 랜덤 문자열이다.
         */
        String refreshToken = refreshTokenUtil.createRefreshToken();

        /*
         * Redis에 Refresh Token 저장
         *
         * RT:{userIdx}            -> refreshToken
         * RT:token:{refreshToken} -> userIdx
         */
        String rtTokenKey = createRefreshTokenReverseKey(refreshToken);

        redisTemplate.opsForValue().set(
                rtKey,
                refreshToken,
                refreshTokenTtlSeconds,
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                rtTokenKey,
                String.valueOf(user.getUsersIdx()),
                refreshTokenTtlSeconds,
                TimeUnit.SECONDS
        );

        // 응답 DTO 생성
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);

        return loginDTO;
    }

    /**
     * 로그아웃을 수행합니다.
     * Refresh Token은 JWT가 아니므로 JWTUtil로 검증하지 않고,
     * Redis에 저장된 Refresh Token과 비교하여 검증합니다.
     *
     * @param logoutReq 로그아웃 요청 정보
     * @return 로그아웃 결과 메시지
     */
    @Override
    @Transactional
    public String logout(LogoutReq logoutReq) {
        if (logoutReq == null || !StringUtils.hasText(logoutReq.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh Token이 존재하지 않습니다.");
        }

        String refreshToken = logoutReq.getRefreshToken().trim();

        String blacklistKey = createBlacklistKey(refreshToken);

        // 이미 로그아웃 처리된 Refresh Token인지 확인
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            throw new IllegalArgumentException("이미 로그아웃 처리된 토큰입니다.");
        }

        /*
         * Refresh Token만으로 userIdx를 찾기 위한 역방향 Redis Key 조회
         *
         * RT:token:{refreshToken} -> userIdx
         */
        String rtTokenKey = createRefreshTokenReverseKey(refreshToken);
        Object userIdxValue = redisTemplate.opsForValue().get(rtTokenKey);

        if (userIdxValue == null) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        Long userIdx;
        try {
            userIdx = Long.valueOf(String.valueOf(userIdxValue));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Refresh Token 사용자 정보가 올바르지 않습니다.");
        }

        /*
         * 사용자 기준 Refresh Token 조회
         *
         * RT:{userIdx} -> refreshToken
         */
        String rtKey = createRefreshTokenKey(userIdx);
        Object savedRefreshTokenValue = redisTemplate.opsForValue().get(rtKey);

        if (savedRefreshTokenValue == null) {
            redisTemplate.delete(rtTokenKey);
            throw new IllegalArgumentException("만료되었거나 존재하지 않는 Refresh Token입니다.");
        }

        String savedRefreshToken = String.valueOf(savedRefreshTokenValue);

        // 요청 Refresh Token과 서버 저장 Refresh Token 비교
        if (!refreshToken.equals(savedRefreshToken)) {
            throw new IllegalArgumentException("Refresh Token 정보가 일치하지 않습니다.");
        }

        /*
         * 삭제 전 남은 TTL 조회
         * blacklist는 원래 Refresh Token이 살아있던 남은 시간 동안만 유지한다.
         */
        Long remainingTtlSeconds = redisTemplate.getExpire(rtKey, TimeUnit.SECONDS);
        long blacklistTtlSeconds = resolvePositiveTtl(
                remainingTtlSeconds,
                refreshTokenUtil.getRefreshTokenExpirationSeconds()
        );

        // Refresh Token 삭제
        redisTemplate.delete(rtKey);
        redisTemplate.delete(rtTokenKey);

        // Refresh Token 재사용 방지를 위해 blacklist 등록
        redisTemplate.opsForValue().set(
                blacklistKey,
                "logout",
                blacklistTtlSeconds,
                TimeUnit.SECONDS
        );

        return "로그아웃 되었습니다.";
    }

    private String createRefreshTokenKey(Long userIdx) {
        return rtPrefix + userIdx;
    }

    private String createRefreshTokenReverseKey(String refreshToken) {
        return rtPrefix + "token:" + refreshToken;
    }

    private String createBlacklistKey(String refreshToken) {
        return blPrefix + refreshToken;
    }

    private long resolvePositiveTtl(Long ttlSeconds, long fallbackTtlSeconds) {
        if (ttlSeconds != null && ttlSeconds > 0) {
            return ttlSeconds;
        }

        return fallbackTtlSeconds;
    }
}