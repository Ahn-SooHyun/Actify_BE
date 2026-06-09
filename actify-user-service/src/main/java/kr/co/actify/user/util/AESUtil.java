package kr.co.actify.user.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AESUtil {

    @Value("${custom.security.aes256.key}")
    private String encodedSecretKey;

    private static final String AES_ALGORITHM = "AES";
    private static final String GCM_TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int AES_256_KEY_SIZE_BYTES = 32;
    private static final int GCM_IV_SIZE_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_TAG_SIZE_BYTES = GCM_TAG_LENGTH_BITS / 8;

    private static final byte FORMAT_VERSION = 1;
    private static final int VERSION_SIZE_BYTES = 1;

    /*
     * AAD는 암호화되지는 않지만 인증 태그 검증에 포함된다.
     * 이 값이 바뀌면 기존 암호문은 복호화할 수 없다.
     */
    private static final byte[] AAD =
            "actify:user-personal-data:aes-gcm:v1".getBytes(StandardCharsets.UTF_8);

    private static final Base64.Encoder BASE64_URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();

    private static final Base64.Decoder BASE64_URL_DECODER =
            Base64.getUrlDecoder();

    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKeySpec secretKeySpec;

    @PostConstruct
    public void init() {
        byte[] keyBytes = decodeBase64Key(encodedSecretKey);

        if (keyBytes.length != AES_256_KEY_SIZE_BYTES) {
            throw new IllegalStateException(
                    "AES-256 키는 Base64 디코딩 후 정확히 32바이트여야 합니다. 현재 길이: "
                            + keyBytes.length + " bytes"
            );
        }

        this.secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);

        // 원본 키 byte 배열은 SecretKeySpec 생성 후 메모리에서 덮어쓴다.
        Arrays.fill(keyBytes, (byte) 0);
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            byte[] iv = generateIv();

            Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKeySpec,
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            );
            cipher.updateAAD(AAD);

            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] cipherTextWithTag = cipher.doFinal(plainBytes);

            ByteBuffer buffer = ByteBuffer.allocate(
                    VERSION_SIZE_BYTES + GCM_IV_SIZE_BYTES + cipherTextWithTag.length
            );

            buffer.put(FORMAT_VERSION);
            buffer.put(iv);
            buffer.put(cipherTextWithTag);

            return BASE64_URL_ENCODER.encodeToString(buffer.array());

        } catch (GeneralSecurityException e) {
            throw new CryptoException("개인정보 암호화에 실패했습니다.", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }

        if (encryptedText.isBlank()) {
            throw new CryptoException("암호문이 비어 있습니다.");
        }

        try {
            byte[] combined = BASE64_URL_DECODER.decode(encryptedText);
            validateCipherTextLength(combined);

            ByteBuffer buffer = ByteBuffer.wrap(combined);

            byte version = buffer.get();
            validateVersion(version);

            byte[] iv = new byte[GCM_IV_SIZE_BYTES];
            buffer.get(iv);

            byte[] cipherTextWithTag = new byte[buffer.remaining()];
            buffer.get(cipherTextWithTag);

            Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKeySpec,
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            );
            cipher.updateAAD(AAD);

            byte[] plainBytes = cipher.doFinal(cipherTextWithTag);

            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new CryptoException("암호문 형식이 올바르지 않습니다.", e);

        } catch (AEADBadTagException e) {
            // 키, IV, AAD, 암호문 중 하나라도 다르거나 변조되면 발생한다.
            throw new CryptoException("암호문 검증에 실패했습니다.", e);

        } catch (GeneralSecurityException e) {
            throw new CryptoException("개인정보 복호화에 실패했습니다.", e);
        }
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_SIZE_BYTES];
        secureRandom.nextBytes(iv);
        return iv;
    }

    private void validateCipherTextLength(byte[] combined) {
        int minimumLength = VERSION_SIZE_BYTES + GCM_IV_SIZE_BYTES + GCM_TAG_SIZE_BYTES;

        if (combined.length < minimumLength) {
            throw new CryptoException("암호문 길이가 올바르지 않습니다.");
        }
    }

    private void validateVersion(byte version) {
        if (version != FORMAT_VERSION) {
            throw new CryptoException("지원하지 않는 암호문 형식입니다. version=" + version);
        }
    }

    private byte[] decodeBase64Key(String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException("AES-256 키 설정값이 비어 있습니다.");
        }

        String trimmedKey = encodedKey.trim();

        try {
            return Base64.getDecoder().decode(trimmedKey);
        } catch (IllegalArgumentException ignored) {
            try {
                return Base64.getUrlDecoder().decode(trimmedKey);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                        "AES-256 키는 Base64 또는 Base64Url 형식이어야 합니다.",
                        e
                );
            }
        }
    }

    public static class CryptoException extends RuntimeException {

        public CryptoException(String message) {
            super(message);
        }

        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}