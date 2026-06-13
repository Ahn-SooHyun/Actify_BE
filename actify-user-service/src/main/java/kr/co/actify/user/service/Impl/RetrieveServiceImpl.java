package kr.co.actify.user.service.Impl;

import kr.co.actify.user.dao.UserInformationRepository;
import kr.co.actify.user.dao.UserVerificationsRepository;
import kr.co.actify.user.global.security.hash.HmacSha256HashUtil;
import kr.co.actify.user.global.security.jwt.RefreshTokenUtil;
import kr.co.actify.user.model.dto.mail.EmailMessage;
import kr.co.actify.user.model.dto.retrieve.FindIDFirstStepDTO;
import kr.co.actify.user.model.dto.retrieve.FindIDSecondStepReq;
import kr.co.actify.user.model.dto.retrieve.FindPWFirstStepDTO;
import kr.co.actify.user.model.dto.retrieve.FindPWSecondStepReq;
import kr.co.actify.user.model.entity.Users;
import kr.co.actify.user.model.entity.UsersInformation;
import kr.co.actify.user.model.entity.UsersVerifications;
import kr.co.actify.user.model.vo.PublicDel;
import kr.co.actify.user.model.vo.UsersVerificationsPurPose;
import kr.co.actify.user.model.vo.UsersVerificationsStatus;
import kr.co.actify.user.service.RetrieveService;
import kr.co.actify.user.service.UserQueryService;
import kr.co.actify.user.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * RetrieveService 인터페이스의 구현체입니다.
 * 아이디 찾기 및 비밀번호 재설정 관련 비즈니스 로직을 처리합니다.
 * 이메일 인증을 통한 본인 확인 절차를 포함합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetrieveServiceImpl implements RetrieveService {
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserQueryService userQueryService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final HmacSha256HashUtil hmacSha256HashUtil;

    // 랜덤 코드 생성, 메일 발송, 비밀번호 암호화 등 유틸리티 주입
    private final RandomCodeUtil randomCodeUtil;
    private final AESUtil aesUtil;
    private final MailUtil mailUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;
    private final RefreshTokenUtil refreshTokenUtil;

    // application.yml에서 설정된 인증번호 유효 시간 (분 단위)
    @Value("${custom.security.verification.retrieve-expiration-minutes}")
    private long expirationMinutes;

    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix; // Refresh Token Key 접두사

    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix; // Blacklist Key 접두사

    /**
     * [아이디 찾기 1단계]
     * 입력받은 이메일로 가입된 정보가 있는지 확인하고, 인증 메일을 발송합니다.
     * @param mail 사용자가 입력한 이메일
     * @return 1단계 결과 DTO (이메일, 인증 만료 시간)
     */
    @Override
    @Transactional // 인증 정보 저장 및 메일 발송을 위해 트랜잭션 적용
    public FindIDFirstStepDTO findIdFirst(String mail) {
        // 이메일로 활성 상태인 사용자 정보 조회
        UsersInformation info = userInformationRepository.findByMailHashAndDel(hmacSha256HashUtil.mailHash(mail), PublicDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        // 사용자 계정 상태 확인 (탈퇴하거나 정지된 계정인지 체크)
        userQueryService.findActiveUser(info.getUsersIdx());

        // 아이디 찾기 목적(FIND_ID)의 인증 정보 생성 및 저장
        UsersVerifications usersVerifications = UsersVerifications.builder()
                .usersIdx(info.getUsersIdx())
                .purpose(UsersVerificationsPurPose.FIND_ID)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(usersVerifications);

        String decryptedMail = aesUtil.decrypt(info.getMailEnc());

        // 인증 이메일 메시지 구성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(decryptedMail)
                .subject("[Actify] ID 찾기 인증번호 안내해 드립니다.")
                .message(emailTemplateProvider.getFindIdTemplate(usersVerifications.getCode()))
                .build();

        // 트랜잭션 커밋이 성공한 직후에 이메일을 발송하도록 설정
        // (DB 저장이 실패했는데 메일이 발송되는 것을 방지)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(emailMessage, true); }
        });

        // 결과 반환
        FindIDFirstStepDTO response = new FindIDFirstStepDTO();
        response.setMail(decryptedMail);
        response.setCertificationTime(usersVerifications.getExpiresAt());
        return response;
    }

    /**
     * [아이디 찾기 2단계]
     * 이메일로 전송된 인증 코드를 검증하고, 성공 시 아이디를 반환합니다.
     * @param req 이메일과 인증 코드가 담긴 요청 객체
     * @return 사용자 아이디
     */
    @Override
    @Transactional
    public String findIdSecond(FindIDSecondStepReq req) {
        UsersInformation info = userInformationRepository
                .findByMailHashAndDel(
                        hmacSha256HashUtil.mailHash(req.getMail()),
                        PublicDel.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        Users user = userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndPurposeAndStatusAndDelOrderByCreatedAtDesc(
                        info.getUsersIdx(),
                        UsersVerificationsPurPose.FIND_ID,
                        UsersVerificationsStatus.PENDING,
                        PublicDel.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        validateVerification(
                verification,
                req.getAuthCode(),
                UsersVerificationsPurPose.FIND_ID
        );

        verification.confirmVerification();
        Long usersIdx = user.getUsersIdx();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invalidateRefreshToken(usersIdx, "ID_FOUND");
                log.error("아이디 찾기 중 로그인 상황의 사용자 발견");
            }
        });


        return user.getId();
    }

    /**
     * [비밀번호 찾기 1단계]
     * 이메일로 가입 정보를 확인하고, 비밀번호 재설정용 인증 메일을 발송합니다.
     * @param mail 사용자가 입력한 이메일
     * @return 1단계 결과 DTO
     */
    @Override
    @Transactional
    public FindPWFirstStepDTO findPwFirst(String mail) {
        // 이메일로 사용자 정보 조회
        UsersInformation info = userInformationRepository.findByMailHashAndDel(hmacSha256HashUtil.mailHash(mail), PublicDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        // 사용자 계정 상태 확인
        userQueryService.findActiveUser(info.getUsersIdx());

        // 비밀번호 재설정 목적(RESET_PW)의 인증 정보 생성 및 저장
        UsersVerifications usersVerifications = UsersVerifications.builder()
                .usersIdx(info.getUsersIdx())
                .purpose(UsersVerificationsPurPose.RESET_PW)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(usersVerifications);

        String decryptedMail = aesUtil.decrypt(info.getMailEnc());

        // 인증 이메일 메시지 구성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(decryptedMail)
                .subject("[Actify] 비밀번호 재설정 인증번호 안내해 드립니다.")
                .message(emailTemplateProvider.getResetPasswordTemplate(usersVerifications.getCode()))
                .build();

        // 트랜잭션 커밋이 성공한 직후에 이메일을 발송하도록 설정
        // (DB 저장이 실패했는데 메일이 발송되는 것을 방지)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(emailMessage, true); }
        });

        // 결과 반환
        FindPWFirstStepDTO response = new FindPWFirstStepDTO();
        response.setMail(decryptedMail);
        response.setCertificationTime(usersVerifications.getExpiresAt());
        return response;
    }

    /**
     * [비밀번호 찾기 2단계]
     * 인증 코드를 검증하고, 새로운 비밀번호로 변경합니다.
     * @param req 이메일, 인증 코드, 새 비밀번호 정보
     * @return 결과 메시지
     */
    @Override
    @Transactional
    public String findPwSecond(FindPWSecondStepReq req) {
        UsersInformation info = userInformationRepository
                .findByMailHashAndDel(
                        hmacSha256HashUtil.mailHash(req.getMail()),
                        PublicDel.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        Users user = userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndPurposeAndStatusAndDelOrderByCreatedAtDesc(
                        info.getUsersIdx(),
                        UsersVerificationsPurPose.RESET_PW,
                        UsersVerificationsStatus.PENDING,
                        PublicDel.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        validateVerification(
                verification,
                req.getAuthCode(),
                UsersVerificationsPurPose.RESET_PW
        );

        if (!Objects.equals(req.getNewPW(), req.getNewPWCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.changePassword(bCryptUtil.encode(req.getNewPW()));
        verification.confirmVerification();
        Long usersIdx = user.getUsersIdx();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invalidateRefreshToken(usersIdx, "PASSWORD_RESET");
                log.error("비밀번호 제설정 중 로그인 상황의 사용자 발견");
            }
        });

        return "비밀번호 재설정이 정상 처리되었습니다.";
    }

    private void validateVerification(
            UsersVerifications verification,
            String code,
            UsersVerificationsPurPose purpose
    ) {
        if (verification.getStatus() != UsersVerificationsStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 인증번호입니다.");
        }

        if (verification.getPurpose() != purpose) {
            throw new IllegalArgumentException("잘못된 인증 요청입니다.");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("만료된 인증번호입니다.");
        }

        if (!Objects.equals(verification.getCode(), code)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
    }

    private void invalidateRefreshToken(Long usersIdx, String reason) {
        String rtKey = createRefreshTokenKey(usersIdx);

        Object refreshTokenValue = redisTemplate.opsForValue().get(rtKey);

        if (refreshTokenValue == null) {
            return;
        }

        String refreshToken = String.valueOf(refreshTokenValue);
        String rtTokenKey = createRefreshTokenReverseKey(refreshToken);

        Long remainingTtlSeconds = redisTemplate.getExpire(rtKey, TimeUnit.SECONDS);
        long blacklistTtlSeconds = resolvePositiveTtl(
                remainingTtlSeconds,
                refreshTokenUtil.getRefreshTokenExpirationSeconds()
        );

        redisTemplate.delete(rtKey);
        redisTemplate.delete(rtTokenKey);

        redisTemplate.opsForValue().set(
                createBlacklistKey(refreshToken),
                reason,
                blacklistTtlSeconds,
                TimeUnit.SECONDS
        );
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