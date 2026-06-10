package kr.co.actify.user.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "custom.security.refresh-token")
public class RefreshTokenProperties {

    /**
     * Refresh Token 만료 시간
     * 예: P14D = 14일
     */
    private Duration expiration = Duration.ofDays(14);

    /**
     * Refresh Token 랜덤 바이트 길이
     * 32 이상 권장, 64 추천
     */
    private int byteLength = 64;

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public int getByteLength() {
        return byteLength;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }
}