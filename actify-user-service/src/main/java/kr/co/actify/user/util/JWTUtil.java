package kr.co.actify.user.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JWTUtil {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String ROLE_CLAIM = "role";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey accessKey;
    private final Duration accessTokenExpiration;
    private final JwtParser accessTokenParser;

    public JWTUtil(JwtProperties jwtProperties) {
        validateProperties(jwtProperties);

        this.accessKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtProperties.getSecret())
        );

        this.accessTokenExpiration = jwtProperties.getExpiration();

        this.accessTokenParser = Jwts.parser()
                .verifyWith(accessKey)
                .clockSkewSeconds(jwtProperties.getClockSkewSeconds())
                .build();
    }

    /**
     * Access Token JWT 생성
     *
     * @param userIdx 사용자 PK
     * @param role 사용자 권한
     * @return Access Token JWT
     */
    public String createAccessToken(Long userIdx, String role) {
        if (userIdx == null) {
            throw new IllegalArgumentException("userIdx는 null일 수 없습니다.");
        }

        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("role은 비어 있을 수 없습니다.");
        }

        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userIdx))
                .id(UUID.randomUUID().toString())
                .claim(ROLE_CLAIM, role)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(accessKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Access Token 검증
     *
     * @param token Access Token 또는 Bearer Token
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = getAccessClaims(token);
            return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Access Token Claims 추출
     *
     * @param token Access Token 또는 Bearer Token
     * @return Claims
     */
    public Claims getAccessClaims(String token) {
        String resolvedToken = resolveToken(token);

        Claims claims = accessTokenParser
                .parseSignedClaims(resolvedToken)
                .getPayload();

        validateAccessTokenType(claims);

        return claims;
    }

    /**
     * Access Token에서 userIdx 추출
     */
    public Long getUserIdxFromAccessToken(String token) {
        return Long.parseLong(getAccessClaims(token).getSubject());
    }

    /**
     * Access Token에서 role 추출
     */
    public String getRoleFromAccessToken(String token) {
        return getAccessClaims(token).get(ROLE_CLAIM, String.class);
    }

    /**
     * Access Token에서 jti 추출
     */
    public String getJtiFromAccessToken(String token) {
        return getAccessClaims(token).getId();
    }

    /**
     * Access Token 만료 시간 추출
     */
    public Date getExpirationFromAccessToken(String token) {
        return getAccessClaims(token).getExpiration();
    }

    /**
     * Access Token 남은 만료 시간(ms) 계산
     * Redis blacklist TTL 설정 시 사용 가능
     */
    public long getRemainingExpirationMillis(String token) {
        Date expiration = getExpirationFromAccessToken(token);
        return Math.max(0, expiration.getTime() - System.currentTimeMillis());
    }

    /**
     * Access Token 만료 시간 초 단위 반환
     * 로그인 응답 expiresIn 값으로 사용 가능
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration.toSeconds();
    }

    /**
     * Authorization Header의 Bearer prefix 제거
     */
    public String resolveToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("토큰이 비어 있습니다.");
        }

        if (token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length()).trim();
        }

        return token.trim();
    }

    private void validateAccessTokenType(Claims claims) {
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

        if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
            throw new JwtException("Access Token이 아닙니다.");
        }
    }

    private void validateProperties(JwtProperties jwtProperties) {
        if (jwtProperties == null) {
            throw new IllegalStateException("JWT 설정이 존재하지 않습니다.");
        }

        if (!StringUtils.hasText(jwtProperties.getSecret())) {
            throw new IllegalStateException("JWT Access Secret이 설정되지 않았습니다.");
        }

        if (jwtProperties.getExpiration() == null || jwtProperties.getExpiration().isZero() || jwtProperties.getExpiration().isNegative()) {
            throw new IllegalStateException("JWT Access Token 만료 시간이 올바르지 않습니다.");
        }

        if (jwtProperties.getClockSkewSeconds() < 0) {
            throw new IllegalStateException("JWT Clock Skew는 음수일 수 없습니다.");
        }
    }
}