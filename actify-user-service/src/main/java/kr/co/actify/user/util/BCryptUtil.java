package kr.co.actify.user.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 비밀번호 해싱 및 검증을 담당하는 유틸리티 클래스입니다.
 *
 * 사용 위치:
 * - 회원가입: 평문 비밀번호를 BCrypt 해시로 변환
 * - 로그인: 입력 비밀번호와 DB 저장 해시 비교
 * - 비밀번호 변경: 새 비밀번호를 BCrypt 해시로 변환
 * - 회원 탈퇴 전 비밀번호 재확인
 *
 * 이 클래스가 직접 BCryptPasswordEncoder를 생성하지 않는 이유:
 * - 비밀번호 해싱 정책은 PasswordEncoderConfig에서 중앙 관리합니다.
 * - 이 클래스는 "설정"이 아니라 "사용"만 담당합니다.
 * - 나중에 BCrypt에서 Argon2id 등으로 바꾸더라도 이 클래스 수정이 줄어듭니다.
 *
 * 보안 주의:
 * - rawPassword는 절대 로그로 출력하지 마세요.
 * - encodedPassword도 가능하면 로그로 출력하지 마세요.
 * - 비밀번호 검증 실패 사유를 사용자에게 자세히 알려주지 마세요.
 */
@Component
public class BCryptUtil {

    /**
     * BCrypt는 입력 비밀번호를 최대 72 bytes까지만 처리하는 특성이 있습니다.
     *
     * 예:
     * - 영어/숫자: 보통 1글자 = 1 byte
     * - 한글: UTF-8 기준 보통 1글자 = 3 bytes
     * - 이모지: UTF-8 기준 보통 4 bytes 이상
     *
     * 만약 72 bytes를 초과하는 비밀번호를 그대로 허용하면
     * 뒤쪽 문자가 BCrypt 검증에 제대로 반영되지 않을 수 있습니다.
     *
     * 그래서 회원가입/비밀번호 변경 시 72 bytes를 초과하면 거부합니다.
     */
    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;

    /**
     * Spring Bean으로 등록된 PasswordEncoder입니다.
     *
     * 실제 구현체:
     * - PasswordEncoderConfig에서 등록한 DelegatingPasswordEncoder
     *
     * 실제 동작:
     * - encode() 호출 시 BCrypt로 해싱
     * - matches() 호출 시 저장된 해시의 "{bcrypt}" 접두사를 보고 BCrypt로 검증
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 생성자 주입입니다.
     *
     * @Autowired를 필드에 직접 붙이지 않고 생성자 주입을 사용하는 이유:
     * - 의존성이 명확합니다.
     * - 테스트가 쉽습니다.
     * - final 필드로 불변성을 유지할 수 있습니다.
     *
     * Spring은 이 클래스가 @Component이므로 자동으로 Bean으로 등록하고,
     * 생성자의 PasswordEncoder 파라미터에 PasswordEncoderConfig의 Bean을 주입합니다.
     *
     * @param passwordEncoder Spring Bean으로 등록된 PasswordEncoder
     */
    public BCryptUtil(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 평문 비밀번호를 단방향 해시값으로 변환합니다.
     *
     * 사용 위치:
     * - 회원가입
     * - 비밀번호 변경
     * - 임시 비밀번호 저장
     *
     * 처리 흐름:
     * 1. null, blank, 72 bytes 초과 여부를 검증합니다.
     * 2. PasswordEncoder를 통해 BCrypt 해시를 생성합니다.
     * 3. 생성된 해시 문자열을 반환합니다.
     *
     * 반환 예시:
     * {bcrypt}$2a$12$w7vQx...
     *
     * 주의:
     * - 반환값만 DB의 Users.PW 컬럼에 저장하세요.
     * - rawPassword는 DB, 로그, 응답에 절대 남기면 안 됩니다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @return DB에 저장할 비밀번호 해시값
     */
    public String encode(String rawPassword) {
        validateRawPasswordForEncode(rawPassword);
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 평문 비밀번호와 DB에 저장된 해시값이 일치하는지 검증합니다.
     *
     * 사용 위치:
     * - 로그인
     * - 비밀번호 재확인
     * - 회원 탈퇴 전 비밀번호 확인
     *
     * 처리 흐름:
     * 1. rawPassword와 encodedPassword가 검증 가능한 값인지 확인합니다.
     * 2. PasswordEncoder.matches()로 검증합니다.
     * 3. 해시 형식 오류 등 예외가 발생하면 false를 반환합니다.
     *
     * 왜 예외를 던지지 않고 false를 반환하는가?
     * - 로그인 검증 과정에서 내부 해시 형식 오류를 사용자에게 노출할 필요가 없습니다.
     * - 사용자에게는 "아이디 또는 비밀번호가 올바르지 않습니다" 정도로만 응답하는 것이 안전합니다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encodedPassword DB에 저장된 비밀번호 해시값
     * @return 일치하면 true, 아니면 false
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (!isValidForMatch(rawPassword, encodedPassword)) {
            return false;
        }

        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            /*
             * DelegatingPasswordEncoder는 저장된 해시값에 "{bcrypt}" 같은 접두사가 없거나,
             * 접두사에 해당하는 PasswordEncoder가 없거나,
             * 해시 형식이 잘못된 경우 IllegalArgumentException을 던질 수 있습니다.
             *
             * 로그인 과정에서는 상세 사유를 외부로 노출하지 않고 false 처리합니다.
             */
            return false;
        }
    }

    /**
     * 저장된 비밀번호 해시가 현재 보안 정책 기준보다 약한지 확인합니다.
     *
     * 사용 예:
     * - 예전에는 BCrypt strength 10으로 저장했음
     * - 현재는 BCrypt strength 12로 올렸음
     * - 사용자가 로그인에 성공하면 기존 해시를 strength 12로 재해싱해서 업데이트
     *
     * 처리 흐름:
     * 1. 로그인 성공
     * 2. needsRehash(users.getPw()) 호출
     * 3. true이면 encode(rawPassword)로 새 해시 생성
     * 4. Users.PW 업데이트
     *
     * @param encodedPassword DB에 저장된 기존 비밀번호 해시값
     * @return 재해싱이 필요하면 true
     */
    public boolean needsRehash(String encodedPassword) {
        if (!hasText(encodedPassword)) {
            return false;
        }

        try {
            return passwordEncoder.upgradeEncoding(encodedPassword);
        } catch (IllegalArgumentException e) {
            /*
             * 해시 형식이 잘못되어 재해싱 필요 여부를 판단할 수 없는 경우입니다.
             * 운영 환경에서는 해당 계정에 비밀번호 재설정 유도를 고려할 수 있습니다.
             */
            return false;
        }
    }

    /**
     * 회원가입/비밀번호 변경 시 해싱 전에 평문 비밀번호를 검증합니다.
     *
     * 여기서 수행하는 검증:
     * - null 금지
     * - 공백만 있는 값 금지
     * - BCrypt 72 bytes 초과 금지
     *
     * 여기서 수행하지 않는 검증:
     * - 최소 8자 이상
     * - 대문자/소문자/숫자/특수문자 정책
     * - ID와 동일한 비밀번호 금지
     * - 이메일과 동일한 비밀번호 금지
     * - 흔한 비밀번호 차단
     *
     * 위 정책 검증은 Service 또는 별도 PasswordPolicyValidator에서 처리하는 것이 좋습니다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     */
    private void validateRawPasswordForEncode(String rawPassword) {
        if (!hasText(rawPassword)) {
            throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
        }

        if (getByteLength(rawPassword) > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new IllegalArgumentException("비밀번호가 허용 가능한 최대 길이를 초과했습니다.");
        }
    }

    /**
     * 로그인 검증 전에 입력값이 검증 가능한 상태인지 확인합니다.
     *
     * encode()와 다르게 matches()에서는 예외를 던지지 않고 false를 반환합니다.
     *
     * 이유:
     * - 로그인 API에서 실패 사유를 자세히 나누면 계정 존재 여부나 내부 정책이 노출될 수 있습니다.
     * - 그래서 검증 불가능한 입력은 단순히 false로 처리합니다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encodedPassword DB에 저장된 해시값
     * @return 검증 가능한 값이면 true
     */
    private boolean isValidForMatch(String rawPassword, String encodedPassword) {
        if (!hasText(rawPassword) || !hasText(encodedPassword)) {
            return false;
        }

        return getByteLength(rawPassword) <= MAX_BCRYPT_PASSWORD_BYTES;
    }

    /**
     * 문자열의 UTF-8 byte 길이를 계산합니다.
     *
     * BCrypt의 72 제한은 Java 문자열 길이 기준이 아니라 byte 기준으로 보는 것이 안전합니다.
     *
     * @param value 검사할 문자열
     * @return UTF-8 기준 byte 길이
     */
    private int getByteLength(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * 문자열이 null이 아니고 공백만으로 구성되지 않았는지 확인합니다.
     *
     * @param value 검사할 문자열
     * @return 실제 문자가 하나 이상 있으면 true
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}