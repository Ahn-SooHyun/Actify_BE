package kr.co.actify.user.global.security.jwt;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class RefreshTokenUtil {

    private static final int MIN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom;
    private final RefreshTokenProperties refreshTokenProperties;

    public RefreshTokenUtil(RefreshTokenProperties refreshTokenProperties) {
        validateProperties(refreshTokenProperties);

        this.secureRandom = new SecureRandom();
        this.refreshTokenProperties = refreshTokenProperties;
    }

    /**
     * SecureRandom 기반 Refresh Token 생성
     *
     * JWT가 아니라 예측 불가능한 랜덤 문자열이다.
     * 클라이언트에는 원본을 반환하고, 서버 DB에는 Hash만 저장하는 것을 권장한다.
     */
    public String createRefreshToken() {
        byte[] randomBytes = new byte[refreshTokenProperties.getByteLength()];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    /**
     * Refresh Token 만료 시간 생성
     *
     * Users_Login.expiresAt 등에 저장할 때 사용한다.
     */
    public LocalDateTime createRefreshTokenExpiresAt() {
        return LocalDateTime.now().plus(refreshTokenProperties.getExpiration());
    }

    /**
     * Refresh Token 만료 시간 초 단위 반환
     *
     * 로그인 응답이나 Redis TTL 계산에 사용할 수 있다.
     */
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenProperties.getExpiration().toSeconds();
    }

    private void validateProperties(RefreshTokenProperties refreshTokenProperties) {
        if (refreshTokenProperties == null) {
            throw new IllegalStateException("Refresh Token 설정이 존재하지 않습니다.");
        }

        if (refreshTokenProperties.getExpiration() == null
                || refreshTokenProperties.getExpiration().isZero()
                || refreshTokenProperties.getExpiration().isNegative()) {
            throw new IllegalStateException("Refresh Token 만료 시간이 올바르지 않습니다.");
        }

        if (refreshTokenProperties.getByteLength() < MIN_BYTE_LENGTH) {
            throw new IllegalStateException("Refresh Token byteLength는 최소 32 이상이어야 합니다.");
        }
    }
}