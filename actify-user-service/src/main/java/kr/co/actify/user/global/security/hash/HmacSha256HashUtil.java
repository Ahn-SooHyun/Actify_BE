package kr.co.actify.user.global.security.hash;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class HmacSha256HashUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secretKey;

    public HmacSha256HashUtil(@Value("${app.hash.secret}") String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException("HMAC_SHA256_SECRET 환경변수가 설정되지 않았습니다.");
        }

        try {
            this.secretKey = Base64.getDecoder().decode(base64Secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("HMAC_SHA256_SECRET 값은 Base64 형식이어야 합니다.", e);
        }

        if (this.secretKey.length < 32) {
            throw new IllegalStateException("HMAC_SHA256_SECRET은 최소 32바이트 이상이어야 합니다.");
        }
    }

    public String mailHash(String email) {
        String normalizedEmail = normalizeEmail(email);
        return hmacSha256Hex("mail:" + normalizedEmail);
    }

    public String phoneNumberHash(String phoneNumber) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        return hmacSha256Hex("phone:" + normalizedPhoneNumber);
    }

    public boolean matchesMail(String rawEmail, String savedMailHash) {
        return constantTimeEquals(mailHash(rawEmail), savedMailHash);
    }

    public boolean matchesPhoneNumber(String rawPhoneNumber, String savedPhoneNumberHash) {
        return constantTimeEquals(phoneNumberHash(rawPhoneNumber), savedPhoneNumberHash);
    }

    private String hmacSha256Hex(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, HMAC_ALGORITHM);
            mac.init(keySpec);

            byte[] hashBytes = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 해시 생성 중 오류가 발생했습니다.", e);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 비어 있을 수 없습니다.");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("전화번호는 비어 있을 수 없습니다.");
        }

        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");

        /*
         * +82 10-1234-5678 형태를 01012345678 형태로 정규화
         * 예:
         * 821012345678 -> 01012345678
         */
        if (digitsOnly.startsWith("82") && digitsOnly.length() >= 11) {
            digitsOnly = "0" + digitsOnly.substring(2);
        }

        if (digitsOnly.isBlank()) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
        }

        return digitsOnly;
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}