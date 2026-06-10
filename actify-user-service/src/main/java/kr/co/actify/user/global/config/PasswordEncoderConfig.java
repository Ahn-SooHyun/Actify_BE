package kr.co.actify.user.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * 비밀번호 해싱 정책을 Spring Bean으로 등록하는 설정 클래스입니다.
 *
 * 이 클래스의 핵심 책임:
 * 1. 프로젝트 전체에서 사용할 PasswordEncoder를 하나의 Bean으로 등록합니다.
 * 2. BCrypt의 보안 강도(strength)를 한 곳에서 관리합니다.
 * 3. 나중에 Argon2id, PBKDF2 등 다른 알고리즘으로 전환할 수 있는 구조를 제공합니다.
 *
 * 왜 별도 Config로 분리하는가?
 * - BCryptUtil 내부에서 new BCryptPasswordEncoder()를 직접 생성하면
 *   BCrypt 강도나 알고리즘 변경 시 유틸 클래스를 직접 수정해야 합니다.
 * - Config에서 Bean으로 관리하면 회원가입, 로그인, 비밀번호 변경,
 *   Spring Security 인증 처리에서 같은 PasswordEncoder를 공유할 수 있습니다.
 *
 * 보안 원칙:
 * - 비밀번호는 복호화 가능한 암호화가 아니라 단방향 해싱으로 저장합니다.
 * - DB에는 원문 비밀번호를 절대 저장하지 않습니다.
 * - AES, RSA 같은 양방향 암호화로 PW를 저장하면 안 됩니다.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt 해싱 강도입니다.
     *
     * BCrypt의 strength는 cost factor라고도 부릅니다.
     * 값이 커질수록 해싱과 검증이 느려지고,
     * 그만큼 공격자가 대량으로 비밀번호를 대입하기 어려워집니다.
     *
     * 일반적인 기준:
     * - 10: 최소 권장 수준
     * - 12: 보안과 성능의 균형이 좋은 실무 기준
     * - 14 이상: 더 강하지만 로그인/회원가입 요청이 많은 서비스에서는 부하가 커질 수 있음
     *
     * Actify 현재 단계에서는 12를 추천합니다.
     */
    private static final int BCRYPT_STRENGTH = 12;

    /**
     * 애플리케이션 전체에서 사용할 PasswordEncoder Bean을 등록합니다.
     *
     * 여기서는 DelegatingPasswordEncoder를 사용합니다.
     *
     * DelegatingPasswordEncoder란?
     * - 저장된 비밀번호 해시 앞의 "{알고리즘ID}"를 보고
     *   어떤 PasswordEncoder로 검증할지 결정하는 Encoder입니다.
     *
     * 예:
     * - {bcrypt}$2a$12$....
     * - {argon2}....
     *
     * 이런 형식이면 나중에 알고리즘을 바꾸더라도
     * 기존 사용자의 비밀번호를 계속 검증할 수 있습니다.
     *
     * 저장 예시:
     * passwordEncoder.encode("1234")
     * → "{bcrypt}$2a$12$..." 형태로 저장됨
     *
     * @return 프로젝트 전체에서 사용할 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        /*
         * 알고리즘 ID와 실제 PasswordEncoder 구현체를 매핑합니다.
         *
         * key:
         * - DB에 저장되는 해시의 접두사 ID입니다.
         *
         * value:
         * - 해당 ID를 처리할 실제 PasswordEncoder입니다.
         */
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        /*
         * bcrypt 알고리즘을 등록합니다.
         *
         * new BCryptPasswordEncoder(BCRYPT_STRENGTH)
         * - 내부적으로 매번 랜덤 salt를 생성합니다.
         * - 해시 결과 안에 salt와 strength 정보가 포함됩니다.
         * - 별도의 Salt 컬럼을 만들 필요가 없습니다.
         */
        encoders.put("bcrypt", new BCryptPasswordEncoder(BCRYPT_STRENGTH));

        /*
         * "bcrypt"를 기본 해싱 알고리즘으로 지정합니다.
         *
         * 이 설정 때문에 passwordEncoder.encode(rawPassword)를 호출하면
         * 결과 앞에 "{bcrypt}" 접두사가 붙습니다.
         */
        return new DelegatingPasswordEncoder("bcrypt", encoders);
    }
}