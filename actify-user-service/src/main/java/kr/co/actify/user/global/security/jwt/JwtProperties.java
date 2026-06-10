package kr.co.actify.user.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "custom.security.jwt.access")
public class JwtProperties {

    /**
     * Access Token 서명에 사용할 Base64 Secret Key
     */
    private String secret;

    /**
     * Access Token 만료 시간
     * 예: PT10M = 10분
     */
    private Duration expiration = Duration.ofMinutes(10);

    /**
     * 서버 간 시간 오차 허용 범위
     */
    private long clockSkewSeconds = 60;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public long getClockSkewSeconds() {
        return clockSkewSeconds;
    }

    public void setClockSkewSeconds(long clockSkewSeconds) {
        this.clockSkewSeconds = clockSkewSeconds;
    }
}